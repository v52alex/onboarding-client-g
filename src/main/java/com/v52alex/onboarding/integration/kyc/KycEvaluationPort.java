package com.v52alex.onboarding.integration.kyc;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public interface KycEvaluationPort {

    EvaluationResult evaluate(UUID caseId, JsonNode answers);

    record EvaluationResult(String provider, Status status, String reference) {
    }

    enum Status {
        APPROVED,
        REVIEW_REQUIRED,
        REJECTED
    }
}
