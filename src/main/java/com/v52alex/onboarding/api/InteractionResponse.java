package com.v52alex.onboarding.api;

import java.util.UUID;
import java.util.Map;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public record InteractionResponse(
    UUID caseId,
    String step,
    String action,
    boolean terminal,
    String status,
    Map<String, String> metadata
) {
}
