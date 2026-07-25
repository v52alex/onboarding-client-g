package com.v52alex.onboarding.api;

import java.util.UUID;

public record InteractionResponse(
    UUID caseId,
    String step,
    String action,
    boolean terminal,
    String status
) {
}

