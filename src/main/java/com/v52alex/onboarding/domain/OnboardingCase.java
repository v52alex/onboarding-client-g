package com.v52alex.onboarding.domain;

import java.time.Instant;
import java.util.UUID;

public record OnboardingCase(
    UUID id,
    String workflowKey,
    String currentStep,
    OnboardingStatus status,
    String data,
    long version,
    Instant createdAt,
    Instant updatedAt
) {
}

