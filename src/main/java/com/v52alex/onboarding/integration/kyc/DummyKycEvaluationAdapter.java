package com.v52alex.onboarding.integration.kyc;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "onboarding.integrations.kyc.provider", havingValue = "dummy")
public class DummyKycEvaluationAdapter implements KycEvaluationPort {

    @Override
    public EvaluationResult evaluate(UUID caseId, JsonNode answers) {
        String scenario = answers.path("dummyScenario").asText("approved").toLowerCase(Locale.ROOT);
        Status status = switch (scenario) {
            case "review" -> Status.REVIEW_REQUIRED;
            case "rejected" -> Status.REJECTED;
            default -> Status.APPROVED;
        };
        return new EvaluationResult("dummy", status, "dummy-kyc-%s".formatted(caseId));
    }
}
