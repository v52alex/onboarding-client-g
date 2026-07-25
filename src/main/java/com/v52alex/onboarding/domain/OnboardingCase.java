package com.v52alex.onboarding.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

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

