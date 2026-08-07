package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.integration.otp.OtpVerificationPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class VerifyContactHandler implements InteractionActionHandler {

    private final OtpVerificationPort otp;

    public VerifyContactHandler(OtpVerificationPort otp) {
        this.otp = otp;
    }

    @Override
    public String action() {
        return "verify-contact";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload, List.of("verificationCode"), List.of(), List.of());
        JsonNode challenge = currentData.path("contactChallenge");
        if (!challenge.isObject() || challenge.path("challengeId").asText().isBlank()) {
            throw new InvalidActionPayloadException("An OTP challenge must be requested before verification");
        }
        OtpVerificationPort.Verification verification = otp.verify(caseId,
            challenge.path("challengeId").asText(), payload.path("verificationCode").asText());
        if (verification.status() != OtpVerificationPort.Status.VERIFIED) {
            throw new InvalidActionPayloadException("The verification code is invalid or expired");
        }
        currentData.putObject("contactVerification")
            .put("channel", challenge.path("channel").asText())
            .put("destination", challenge.path("destination").asText())
            .put("provider", verification.provider())
            .put("status", verification.status().name());
        return ActionOutcome.success(currentData);
    }
}
