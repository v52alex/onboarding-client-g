package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.integration.biometric.BiometricVerificationPort;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Component
public class IdentityVerificationHandler implements InteractionActionHandler {

    private final BiometricVerificationPort biometricVerification;

    public IdentityVerificationHandler(BiometricVerificationPort biometricVerification) {
        this.biometricVerification = biometricVerification;
    }

    @Override
    public String action() {
        return "verify-identity";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload,
            List.of("documentReference", "livenessReference"), List.of(), List.of());
        BiometricVerificationPort.VerificationResult result =
            biometricVerification.verify(caseId, payload);
        currentData.set("identity", payload.deepCopy());
        currentData.putObject("identityVerification")
            .put("provider", result.provider())
            .put("status", result.status().name())
            .put("reference", result.reference());

        String outcome = switch (result.status()) {
            case VERIFIED -> "verified";
            case REJECTED -> "rejected";
            case MANUAL_REVIEW -> "manual-review";
        };
        return new ActionOutcome(outcome, currentData);
    }
}
