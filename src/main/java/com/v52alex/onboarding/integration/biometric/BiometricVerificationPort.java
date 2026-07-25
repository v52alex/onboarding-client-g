package com.v52alex.onboarding.integration.biometric;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public interface BiometricVerificationPort {

    VerificationResult verify(UUID caseId, JsonNode identityData);

    record VerificationResult(String provider, Status status, String reference) {
    }

    enum Status {
        VERIFIED,
        REJECTED,
        MANUAL_REVIEW
    }
}

