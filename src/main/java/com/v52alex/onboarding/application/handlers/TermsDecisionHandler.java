package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import java.util.UUID;

final class TermsDecisionHandler implements InteractionActionHandler {

    @Override
    public String action() {
        return "accept-terms";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            throw new InvalidActionPayloadException("Action payload must be a JSON object");
        }
        JsonNode version = payload.path("documentVersion");
        if (!version.isTextual() || version.asText().isBlank()) {
            throw new InvalidActionPayloadException("Field 'documentVersion' is required");
        }
        JsonNode accepted = payload.path("accepted");
        if (!accepted.isBoolean()) {
            throw new InvalidActionPayloadException("Field 'accepted' must be boolean");
        }
        currentData.set("consent", payload.deepCopy());
        return new ActionOutcome(accepted.asBoolean() ? "success" : "rejected", currentData);
    }
}
