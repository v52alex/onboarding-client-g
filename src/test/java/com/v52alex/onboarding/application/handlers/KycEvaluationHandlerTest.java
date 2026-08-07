package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.domain.OperationalRecords.Document;
import com.v52alex.onboarding.domain.OperationalRecords.FileSet;
import com.v52alex.onboarding.domain.OperationalRecords.CachedAction;
import com.v52alex.onboarding.domain.OperationalRecords.CaseEvent;
import com.v52alex.onboarding.domain.OperationalRecords.Consent;
import com.v52alex.onboarding.integration.kyc.KycEvaluationPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KycEvaluationHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void storesTheProviderEvaluationWithoutChangingTheWorkflowOutcome() {
        KycEvaluationPort port = (caseId, answers) -> new KycEvaluationPort.EvaluationResult(
            "test-provider", KycEvaluationPort.Status.REVIEW_REQUIRED, "kyc-ref");
        UUID caseId = UUID.randomUUID();
        UUID fileSetId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        OnboardingOperations operations = operationsWithDocument(caseId, fileSetId, documentId);
        KycEvaluationHandler handler = new KycEvaluationHandler(port, operations);

        ActionOutcome result = handler.handle(caseId, objectMapper.createObjectNode(),
            objectMapper.createObjectNode()
                .put("documentType", "PASSPORT")
                .put("documentNumber", "P-100")
                .put("issuingCountry", "SV")
                .put("documentId", documentId.toString()));

        assertThat(result.code()).isEqualTo("success");
        assertThat(result.updatedData().at("/kycEvaluation/provider").asText())
            .isEqualTo("test-provider");
        assertThat(result.updatedData().at("/kycEvaluation/status").asText())
            .isEqualTo("REVIEW_REQUIRED");
    }

    @Test
    void rejectsKycWhenTheDocumentDoesNotBelongToTheCase() {
        KycEvaluationPort port = (caseId, answers) -> new KycEvaluationPort.EvaluationResult(
            "test-provider", KycEvaluationPort.Status.APPROVED, "kyc-ref");
        UUID caseId = UUID.randomUUID();
        OnboardingOperations operations = new TestOperations(caseId, List.of(), List.of());
        KycEvaluationHandler handler = new KycEvaluationHandler(port, operations);

        assertThatThrownBy(() -> handler.handle(caseId, objectMapper.createObjectNode(),
            objectMapper.createObjectNode()
                .put("documentType", "PASSPORT")
                .put("documentNumber", "P-100")
                .put("issuingCountry", "SV")
                .put("documentId", UUID.randomUUID().toString())))
            .isInstanceOf(InvalidActionPayloadException.class)
            .hasMessageContaining("available document");
    }

    private OnboardingOperations operationsWithDocument(UUID caseId, UUID fileSetId, UUID documentId) {
        FileSet fileSet = new FileSet(fileSetId, caseId,
            "Identity documents", 1, "application/pdf", Instant.now());
        Document document = new Document(documentId, fileSetId,
            "content-service:" + UUID.randomUUID(), "identity.pdf", "application/pdf", 12,
            "a".repeat(64), "AVAILABLE", Instant.now());
        return new TestOperations(caseId, List.of(fileSet), List.of(document));
    }

    private record TestOperations(UUID caseId, List<FileSet> fileSets, List<Document> documents)
        implements OnboardingOperations {

        @Override public List<FileSet> findFileSets(UUID requestedCaseId) {
            return caseId.equals(requestedCaseId) ? fileSets : List.of();
        }

        @Override public List<Document> findDocuments(UUID fileSetId) {
            return documents.stream().filter(document -> document.fileSetId().equals(fileSetId)).toList();
        }

        @Override public void recordEvent(CaseEvent event, String payload) { throw unsupported(); }
        @Override public void recordConsent(Consent consent) { throw unsupported(); }
        @Override public List<CaseEvent> findEvents(UUID id) { throw unsupported(); }
        @Override public List<Consent> findConsents(UUID id) { throw unsupported(); }
        @Override public Optional<CachedAction> findCachedAction(UUID id, String action, String key) { throw unsupported(); }
        @Override public void saveCachedAction(UUID id, String action, String key, CachedAction actionValue) { throw unsupported(); }
        @Override public FileSet createFileSet(FileSet fileSet) { throw unsupported(); }
        @Override public Optional<FileSet> findFileSet(UUID fileSetId) { throw unsupported(); }
        @Override public Document addDocument(Document document) { throw unsupported(); }

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Not required by this test");
        }
    }
}
