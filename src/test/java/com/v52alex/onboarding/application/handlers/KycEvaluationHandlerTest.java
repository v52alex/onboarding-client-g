package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.integration.kyc.KycEvaluationPort;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KycEvaluationHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void storesTheProviderEvaluationWithoutChangingTheWorkflowOutcome() {
        KycEvaluationPort port = (caseId, answers) -> new KycEvaluationPort.EvaluationResult(
            "test-provider", KycEvaluationPort.Status.REVIEW_REQUIRED, "kyc-ref");
        KycEvaluationHandler handler = new KycEvaluationHandler(port);

        ActionOutcome result = handler.handle(UUID.randomUUID(), objectMapper.createObjectNode(),
            objectMapper.createObjectNode()
                .put("documentType", "PASSPORT")
                .put("documentNumber", "P-100")
                .put("issuingCountry", "SV"));

        assertThat(result.code()).isEqualTo("success");
        assertThat(result.updatedData().at("/kycEvaluation/provider").asText())
            .isEqualTo("test-provider");
        assertThat(result.updatedData().at("/kycEvaluation/status").asText())
            .isEqualTo("REVIEW_REQUIRED");
    }
}
