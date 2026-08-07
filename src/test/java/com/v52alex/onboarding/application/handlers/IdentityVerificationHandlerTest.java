package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.integration.biometric.BiometricVerificationPort;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

class IdentityVerificationHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
        "VERIFIED,verified",
        "REJECTED,rejected",
        "MANUAL_REVIEW,manual-review"
    })
    void mapsProviderStatusesToWorkflowOutcomes(
        BiometricVerificationPort.Status status,
        String expectedOutcome
    ) {
        BiometricVerificationPort port = (caseId, identityData) ->
            new BiometricVerificationPort.VerificationResult("test-provider", status, "ref-123");
        IdentityVerificationHandler handler = new IdentityVerificationHandler(port);
        JsonNode payload = objectMapper.createObjectNode()
            .put("documentReference", "doc-1")
            .put("livenessReference", "liveness-1");

        ActionOutcome result = handler.handle(
            UUID.randomUUID(),
            objectMapper.createObjectNode(),
            payload
        );

        assertThat(result.code()).isEqualTo(expectedOutcome);
        assertThat(result.updatedData().at("/identityVerification/provider").asText())
            .isEqualTo("test-provider");
        assertThat(result.updatedData().at("/identityVerification/reference").asText())
            .isEqualTo("ref-123");
    }
}
