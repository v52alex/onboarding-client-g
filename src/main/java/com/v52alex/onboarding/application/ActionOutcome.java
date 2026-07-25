package com.v52alex.onboarding.application;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record ActionOutcome(String code, ObjectNode updatedData) {

    public static ActionOutcome success(ObjectNode data) {
        return new ActionOutcome("success", data);
    }
}

