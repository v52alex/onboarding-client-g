package com.v52alex.onboarding.integration.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DummyOtpVerificationAdapterTest {

    private final DummyOtpVerificationAdapter adapter = new DummyOtpVerificationAdapter();

    @Test
    void acceptsOnlyTheDeterministicDevelopmentCode() {
        UUID caseId = UUID.randomUUID();
        String challengeId = adapter.request(caseId, "+50370000000").id();

        assertThat(adapter.verify(caseId, challengeId, "123456").status())
            .isEqualTo(OtpVerificationPort.Status.VERIFIED);
        assertThat(adapter.verify(caseId, challengeId, "000000").status())
            .isEqualTo(OtpVerificationPort.Status.INVALID);
        assertThat(adapter.verify(caseId, "dummy-otp-" + UUID.randomUUID(), "123456").status())
            .isEqualTo(OtpVerificationPort.Status.INVALID);
    }
}
