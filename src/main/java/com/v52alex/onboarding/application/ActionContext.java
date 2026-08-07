package com.v52alex.onboarding.application;

public record ActionContext(String idempotencyKey, String actorId, String correlationId) {

    public static ActionContext empty() {
        return new ActionContext(null, null, null);
    }
}
