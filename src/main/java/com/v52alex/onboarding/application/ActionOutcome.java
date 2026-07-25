package com.v52alex.onboarding.application;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public record ActionOutcome(String code, ObjectNode updatedData) {

    public static ActionOutcome success(ObjectNode data) {
        return new ActionOutcome("success", data);
    }
}

