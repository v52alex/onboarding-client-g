package com.v52alex.onboarding.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

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

