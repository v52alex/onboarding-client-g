package com.v52alex.onboarding.integration.biometric;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NoOpBiometricVerificationAdapterTest {

    @Test
    void sendsIdentityVerificationToManualReviewWithoutExternalProvider() {
        NoOpBiometricVerificationAdapter adapter = new NoOpBiometricVerificationAdapter();
        UUID caseId = UUID.randomUUID();

        BiometricVerificationPort.VerificationResult result =
            adapter.verify(caseId, new ObjectMapper().createObjectNode());

        assertThat(result.provider()).isEqualTo("none");
        assertThat(result.status()).isEqualTo(BiometricVerificationPort.Status.MANUAL_REVIEW);
        assertThat(result.reference()).isEqualTo(caseId.toString());
    }
}

