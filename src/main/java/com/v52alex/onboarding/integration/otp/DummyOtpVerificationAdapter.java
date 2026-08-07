package com.v52alex.onboarding.integration.otp;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "onboarding.integrations.otp.provider", havingValue = "dummy")
public class DummyOtpVerificationAdapter implements OtpVerificationPort {

    static final String VALID_CODE = "123456";

    @Override
    public Challenge request(UUID caseId, String destination) {
        return new Challenge("dummy-otp-%s".formatted(caseId), "dummy", 300);
    }

    @Override
    public Verification verify(UUID caseId, String challengeId, String code) {
        Status status = VALID_CODE.equals(code) ? Status.VERIFIED : Status.INVALID;
        return new Verification("dummy", status);
    }
}
