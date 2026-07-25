package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import java.util.UUID;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public class JsonSectionActionHandler implements InteractionActionHandler {

    private final String action;
    private final String section;

    public JsonSectionActionHandler(String action, String section) {
        this.action = action;
        this.section = section;
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        currentData.set(section, payload.deepCopy());
        return ActionOutcome.success(currentData);
    }
}

