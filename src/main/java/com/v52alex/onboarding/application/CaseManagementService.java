package com.v52alex.onboarding.application;

import com.v52alex.onboarding.domain.CaseManagementRecords.CasePage;
import com.v52alex.onboarding.domain.CaseManagementRecords.Review;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewDecision;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewEvent;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewStatus;
import com.v52alex.onboarding.domain.CaseManagementRepository;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseManagementService {

    private final OnboardingOrchestrator onboarding;
    private final CaseManagementRepository reviews;

    public CaseManagementService(OnboardingOrchestrator onboarding, CaseManagementRepository reviews) {
        this.onboarding = onboarding;
        this.reviews = reviews;
    }

    @Transactional(readOnly = true)
    public CasePage search(ReviewStatus status, String assignedTo, int page, int size) {
        return reviews.search(status, assignedTo, page, size);
    }

    @Transactional(readOnly = true)
    public ManagedCase get(UUID caseId) {
        OnboardingCase onboardingCase = onboarding.get(caseId);
        Review review = reviews.findReview(caseId)
            .orElseThrow(() -> new InvalidReviewTransitionException("Case is not ready for operational review"));
        return new ManagedCase(onboardingCase, review, reviews.findEvents(caseId));
    }

    @Transactional
    public ManagedCase assign(UUID caseId, String actorId) {
        requireReviewable(caseId);
        try {
            reviews.assign(caseId, actorId, actorId);
        } catch (IllegalStateException exception) {
            throw new InvalidReviewTransitionException(exception.getMessage());
        }
        return get(caseId);
    }

    @Transactional
    public ManagedCase decide(UUID caseId, ReviewDecision decision, String reason, String actorId) {
        requireReviewable(caseId);
        if (decision == ReviewDecision.REJECTED && (reason == null || reason.isBlank())) {
            throw new InvalidReviewTransitionException("A rejection reason is required");
        }
        try {
            reviews.decide(caseId, ReviewStatus.valueOf(decision.name()), normalize(reason), actorId);
        } catch (IllegalStateException exception) {
            throw new InvalidReviewTransitionException(exception.getMessage());
        }
        return get(caseId);
    }

    private void requireReviewable(UUID caseId) {
        if (onboarding.get(caseId).status() != OnboardingStatus.COMPLETED) {
            throw new InvalidReviewTransitionException("Only completed onboarding cases can be reviewed");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record ManagedCase(OnboardingCase onboardingCase, Review review, List<ReviewEvent> reviewEvents) {
    }
}
