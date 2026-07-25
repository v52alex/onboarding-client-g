package com.v52alex.onboarding.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;

public interface InteractionActionHandler {

    String action();

    ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload);
}

