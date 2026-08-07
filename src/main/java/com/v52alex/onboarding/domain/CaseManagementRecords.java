package com.v52alex.onboarding.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CaseManagementRecords {

    private CaseManagementRecords() {
    }

    public enum ReviewStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum ReviewDecision {
        APPROVED,
        REJECTED
    }

    public record Review(
        UUID caseId, ReviewStatus status, String assignedTo, String decisionReason,
        String decidedBy, Instant decidedAt, Instant createdAt, Instant updatedAt
    ) {
    }

    public record ReviewEvent(
        UUID id, UUID caseId, String eventType, String actorId, String detail, Instant occurredAt
    ) {
    }

    public record CaseSummary(
        UUID id, String workflowKey, String currentStep, OnboardingStatus onboardingStatus,
        ReviewStatus reviewStatus, String assignedTo, Instant createdAt, Instant updatedAt
    ) {
    }

    public record CasePage(
        List<CaseSummary> content, int page, int size, long totalElements, int totalPages
    ) {
    }
}
