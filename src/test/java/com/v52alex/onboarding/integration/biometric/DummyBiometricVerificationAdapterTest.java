package com.v52alex.onboarding.integration.biometric;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DummyBiometricVerificationAdapterTest {

    @Test
    void returnsDeterministicVerifiedResultForE2eEnvironments() {
        UUID caseId = UUID.randomUUID();

        BiometricVerificationPort.VerificationResult result =
            new DummyBiometricVerificationAdapter().verify(caseId, JsonNodeFactory.instance.objectNode());

        assertThat(result.provider()).isEqualTo("dummy");
        assertThat(result.status()).isEqualTo(BiometricVerificationPort.Status.VERIFIED);
        assertThat(result.reference()).isEqualTo("dummy-verified-" + caseId);
    }
}
