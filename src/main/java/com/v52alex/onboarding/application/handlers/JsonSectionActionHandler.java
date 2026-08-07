package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import java.util.UUID;
import java.util.List;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public class JsonSectionActionHandler implements InteractionActionHandler {

    private final String action;
    private final String section;
    private final List<String> requiredText;
    private final List<String> requiredTrue;
    private final List<String> requiredTextArrays;

    public JsonSectionActionHandler(String action, String section, List<String> requiredText,
        List<String> requiredTrue, List<String> requiredTextArrays) {
        this.action = action;
        this.section = section;
        this.requiredText = List.copyOf(requiredText);
        this.requiredTrue = List.copyOf(requiredTrue);
        this.requiredTextArrays = List.copyOf(requiredTextArrays);
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload, requiredText, requiredTrue, requiredTextArrays);
        currentData.set(section, payload.deepCopy());
        return ActionOutcome.success(currentData);
    }
}
