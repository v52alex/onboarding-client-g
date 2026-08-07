package com.v52alex.onboarding.application;

import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.domain.OperationalRecords.Document;
import com.v52alex.onboarding.domain.OperationalRecords.FileSet;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import com.v52alex.onboarding.integration.content.ContentStorageClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    private final OnboardingOrchestrator orchestrator;
    private final OnboardingOperations operations;
    private final ContentStorageClient contentStorage;

    public DocumentService(OnboardingOrchestrator orchestrator, OnboardingOperations operations,
        ContentStorageClient contentStorage) {
        this.orchestrator = orchestrator;
        this.operations = operations;
        this.contentStorage = contentStorage;
    }

    @Transactional
    public FileSet createFileSet(UUID caseId, String name, int maxFiles, List<String> allowedMediaTypes) {
        orchestrator.get(caseId);
        if (maxFiles < 1) {
            throw new DocumentValidationException("maxFiles must be greater than zero");
        }
        return operations.createFileSet(new FileSet(UUID.randomUUID(), caseId, name, maxFiles,
            String.join(",", allowedMediaTypes), Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<FileSet> fileSets(UUID caseId) {
        orchestrator.get(caseId);
        return operations.findFileSets(caseId);
    }

    @Transactional
    public Document register(UUID caseId, UUID fileSetId, String objectKey, String originalFileName,
        String mimeType, long sizeBytes, String checksumSha256) {
        orchestrator.get(caseId);
        FileSet fileSet = operations.findFileSet(fileSetId)
            .filter(candidate -> candidate.caseId().equals(caseId))
            .orElseThrow(() -> new DocumentValidationException("File set does not belong to the case"));
        List<Document> current = operations.findDocuments(fileSetId);
        if (current.size() >= fileSet.maxFiles()) {
            throw new DocumentValidationException("File set has reached its maximum number of files");
        }
        boolean allowed = Arrays.stream(fileSet.allowedMediaTypes().split(","))
            .map(String::trim).anyMatch(mimeType::equalsIgnoreCase);
        if (!allowed) {
            throw new DocumentValidationException("Media type is not allowed for this file set");
        }
        return operations.addDocument(new Document(UUID.randomUUID(), fileSetId, objectKey,
            originalFileName, mimeType, sizeBytes, checksumSha256, "AVAILABLE", Instant.now()));
    }

    @Transactional
    public Document upload(UUID caseId, UUID fileSetId, MultipartFile file, String actorId) {
        FileSet fileSet = requireFileSet(caseId, fileSetId);
        validateCapacityAndMediaType(fileSet, file.getContentType());
        var stored = contentStorage.upload(caseId, file, actorId);
        try {
            return operations.addDocument(new Document(UUID.randomUUID(), fileSetId,
                "content-service:" + stored.id(), stored.originalName(), stored.contentType(),
                stored.sizeBytes(), stored.checksumSha256(), stored.status(), Instant.now()));
        } catch (RuntimeException exception) {
            contentStorage.delete(caseId, stored.id());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public byte[] download(UUID caseId, UUID fileSetId, UUID documentId) {
        requireFileSet(caseId, fileSetId);
        Document document = operations.findDocuments(fileSetId).stream()
            .filter(candidate -> candidate.id().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new DocumentValidationException("Document does not belong to the file set"));
        if (!document.objectKey().startsWith("content-service:")) {
            throw new DocumentValidationException("Document is not managed by Content Service");
        }
        return contentStorage.download(caseId,
            UUID.fromString(document.objectKey().substring("content-service:".length())));
    }

    @Transactional(readOnly = true)
    public List<Document> documents(UUID caseId, UUID fileSetId) {
        requireFileSet(caseId, fileSetId);
        return operations.findDocuments(fileSetId);
    }

    private FileSet requireFileSet(UUID caseId, UUID fileSetId) {
        orchestrator.get(caseId);
        return operations.findFileSet(fileSetId).filter(candidate -> candidate.caseId().equals(caseId))
            .orElseThrow(() -> new DocumentValidationException("File set does not belong to the case"));
    }

    private void validateCapacityAndMediaType(FileSet fileSet, String mimeType) {
        List<Document> current = operations.findDocuments(fileSet.id());
        if (current.size() >= fileSet.maxFiles()) {
            throw new DocumentValidationException("File set has reached its maximum number of files");
        }
        boolean allowed = mimeType != null && Arrays.stream(fileSet.allowedMediaTypes().split(","))
            .map(String::trim).anyMatch(mimeType::equalsIgnoreCase);
        if (!allowed) {
            throw new DocumentValidationException("Media type is not allowed for this file set");
        }
    }
}
