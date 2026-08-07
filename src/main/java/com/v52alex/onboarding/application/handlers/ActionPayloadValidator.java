package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import java.util.List;

final class ActionPayloadValidator {

    private ActionPayloadValidator() {
    }

    static void validate(JsonNode payload, List<String> requiredText,
        List<String> requiredTrue, List<String> requiredTextArrays) {
        if (payload == null || !payload.isObject()) {
            throw new InvalidActionPayloadException("Action payload must be a JSON object");
        }
        requiredText.forEach(field -> {
            JsonNode value = payload.path(field);
            if (!value.isTextual() || value.asText().isBlank()) {
                throw new InvalidActionPayloadException("Field '%s' is required".formatted(field));
            }
        });
        requiredTrue.forEach(field -> {
            if (!payload.path(field).isBoolean() || !payload.path(field).asBoolean()) {
                throw new InvalidActionPayloadException("Field '%s' must be true".formatted(field));
            }
        });
        requiredTextArrays.forEach(field -> {
            JsonNode value = payload.path(field);
            if (!value.isArray() || value.isEmpty()
                || !value.valueStream().allMatch(item -> item.isTextual() && !item.asText().isBlank())) {
                throw new InvalidActionPayloadException(
                    "Field '%s' must be a non-empty string array".formatted(field));
            }
        });
    }
}
