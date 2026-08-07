package com.v52alex.onboarding.integration.otp;

import java.util.UUID;

public interface OtpVerificationPort {

    Challenge request(UUID caseId, String destination);

    Verification verify(UUID caseId, String challengeId, String code);

    record Challenge(String id, String provider, long expiresInSeconds) {
    }

    record Verification(String provider, Status status) {
    }

    enum Status {
        VERIFIED,
        INVALID,
        EXPIRED
    }
}
