package com.v52alex.onboarding.domain;

import com.v52alex.onboarding.domain.CaseManagementRecords.CasePage;
import com.v52alex.onboarding.domain.CaseManagementRecords.Review;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewEvent;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseManagementRepository {

    void ensurePendingReview(UUID caseId);

    CasePage search(ReviewStatus status, String assignedTo, int page, int size);

    Optional<Review> findReview(UUID caseId);

    Review assign(UUID caseId, String assignedTo, String actorId);

    Review decide(UUID caseId, ReviewStatus decision, String reason, String actorId);

    List<ReviewEvent> findEvents(UUID caseId);
}
