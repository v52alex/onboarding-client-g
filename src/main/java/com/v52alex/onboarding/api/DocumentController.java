package com.v52alex.onboarding.api;

import com.v52alex.onboarding.application.DocumentService;
import com.v52alex.onboarding.domain.OperationalRecords.Document;
import com.v52alex.onboarding.domain.OperationalRecords.FileSet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/onboarding-cases/{caseId}/file-sets")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FileSet create(@PathVariable UUID caseId, @Valid @RequestBody CreateFileSetRequest request) {
        return service.createFileSet(caseId, request.name(), request.maxFiles(), request.allowedMediaTypes());
    }

    @GetMapping
    public List<FileSet> list(@PathVariable UUID caseId) {
        return service.fileSets(caseId);
    }

    @PostMapping("/{fileSetId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public Document register(@PathVariable UUID caseId, @PathVariable UUID fileSetId,
        @Valid @RequestBody RegisterDocumentRequest request) {
        return service.register(caseId, fileSetId, request.objectKey(), request.originalFileName(),
            request.mimeType(), request.sizeBytes(), request.checksumSha256());
    }

    @PostMapping(value = "/{fileSetId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Document upload(@PathVariable UUID caseId, @PathVariable UUID fileSetId,
        @RequestParam MultipartFile file,
        @RequestHeader(value = "X-Actor-Id", defaultValue = "system") String actorId) {
        return service.upload(caseId, fileSetId, file, actorId);
    }

    @GetMapping("/{fileSetId}/documents/{documentId}/content")
    public ResponseEntity<byte[]> download(@PathVariable UUID caseId, @PathVariable UUID fileSetId,
        @PathVariable UUID documentId) {
        Document document = service.documents(caseId, fileSetId).stream()
            .filter(candidate -> candidate.id().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new com.v52alex.onboarding.application.DocumentValidationException(
                "Document does not belong to the file set"));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(document.mimeType()))
            .header("Content-Disposition", ContentDisposition.attachment()
                .filename(document.originalFileName(), java.nio.charset.StandardCharsets.UTF_8)
                .build().toString())
            .body(service.download(caseId, fileSetId, documentId));
    }

    @GetMapping("/{fileSetId}/documents")
    public List<Document> documents(@PathVariable UUID caseId, @PathVariable UUID fileSetId) {
        return service.documents(caseId, fileSetId);
    }

    public record CreateFileSetRequest(
        @NotBlank @Size(max = 120) String name,
        @Positive int maxFiles,
        @Size(min = 1, max = 3) List<@NotBlank @Size(max = 150) String> allowedMediaTypes
    ) {
    }

    public record RegisterDocumentRequest(
        @NotBlank @Size(max = 500) String objectKey,
        @NotBlank @Size(max = 255) String originalFileName,
        @NotBlank @Size(max = 150) String mimeType,
        @Positive long sizeBytes,
        @NotBlank @Pattern(regexp = "^[a-fA-F0-9]{64}$") String checksumSha256
    ) {
    }
}
