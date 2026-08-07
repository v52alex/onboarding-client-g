package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.integration.otp.OtpVerificationPort;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RequestOtpHandler implements InteractionActionHandler {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+[1-9][0-9]{7,14}$");
    private final OtpVerificationPort otp;

    public RequestOtpHandler(OtpVerificationPort otp) {
        this.otp = otp;
    }

    @Override
    public String action() {
        return "request-otp";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload, List.of("channel", "destination"), List.of(), List.of());
        String channel = payload.path("channel").asText().toUpperCase(Locale.ROOT);
        String destination = payload.path("destination").asText().trim();
        validateDestination(channel, destination);
        OtpVerificationPort.Challenge challenge = otp.request(caseId, destination);
        currentData.putObject("contactChallenge")
            .put("channel", channel)
            .put("destination", destination)
            .put("challengeId", challenge.id())
            .put("provider", challenge.provider())
            .put("expiresInSeconds", challenge.expiresInSeconds());
        return ActionOutcome.success(currentData);
    }

    private void validateDestination(String channel, String destination) {
        boolean valid = switch (channel) {
            case "EMAIL" -> destination.length() <= 254 && EMAIL.matcher(destination).matches();
            case "PHONE" -> PHONE.matcher(destination).matches();
            default -> false;
        };
        if (!valid) {
            throw new InvalidActionPayloadException(
                "Fields 'channel' and 'destination' must contain a valid EMAIL or PHONE destination");
        }
    }
}
