package com.v52alex.onboarding.integration.biometric;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "onboarding.integrations.biometric.provider",
    havingValue = "dummy"
)
public class DummyBiometricVerificationAdapter implements BiometricVerificationPort {

    @Override
    public VerificationResult verify(UUID caseId, JsonNode identityData) {
        String reference = "dummy-verified-%s".formatted(caseId);
        return new VerificationResult("dummy", Status.VERIFIED, reference);
    }
}
