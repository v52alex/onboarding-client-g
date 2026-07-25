package com.v52alex.onboarding.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public record CaseResponse(
    UUID id,
    String workflowKey,
    String currentStep,
    String status,
    JsonNode data,
    long version,
    Instant createdAt,
    Instant updatedAt
) {
}

