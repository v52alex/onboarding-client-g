package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.integration.kyc.KycEvaluationPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KycEvaluationHandler implements InteractionActionHandler {

    private final KycEvaluationPort evaluationPort;

    public KycEvaluationHandler(KycEvaluationPort evaluationPort) {
        this.evaluationPort = evaluationPort;
    }

    @Override
    public String action() {
        return "submit-kyc";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload,
            List.of("documentType", "documentNumber", "issuingCountry"), List.of(), List.of());
        KycEvaluationPort.EvaluationResult result = evaluationPort.evaluate(caseId, payload);
        currentData.set("kyc", payload.deepCopy());
        currentData.putObject("kycEvaluation")
            .put("provider", result.provider())
            .put("status", result.status().name())
            .put("reference", result.reference());
        return ActionOutcome.success(currentData);
    }
}
