package com.v52alex.onboarding.domain;

import java.time.Instant;
import java.util.UUID;

public final class OperationalRecords {

    private OperationalRecords() {
    }

    public record CaseEvent(
        UUID id, UUID caseId, long sequence, String eventType, String action,
        String previousStep, String resultingStep, String outcome, String actorId,
        String correlationId, String metadata, Instant occurredAt
    ) {
    }

    public record Consent(
        UUID id, UUID caseId, String type, String documentVersion, boolean accepted,
        String actorId, String correlationId, String evidence, Instant acceptedAt
    ) {
    }

    public record CachedAction(String requestHash, OnboardingCase result) {
    }

    public record FileSet(
        UUID id, UUID caseId, String name, int maxFiles, String allowedMediaTypes, Instant createdAt
    ) {
    }

    public record Document(
        UUID id, UUID fileSetId, String objectKey, String originalFileName, String mimeType,
        long sizeBytes, String checksumSha256, String status, Instant createdAt
    ) {
    }
}
