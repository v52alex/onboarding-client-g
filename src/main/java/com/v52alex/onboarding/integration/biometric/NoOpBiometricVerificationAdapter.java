package com.v52alex.onboarding.integration.biometric;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Component
@ConditionalOnProperty(
    name = "onboarding.integrations.biometric.provider",
    havingValue = "none",
    matchIfMissing = true
)
public class NoOpBiometricVerificationAdapter implements BiometricVerificationPort {

    @Override
    public VerificationResult verify(UUID caseId, JsonNode identityData) {
        return new VerificationResult("none", Status.MANUAL_REVIEW, caseId.toString());
    }
}

