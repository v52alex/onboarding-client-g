package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.integration.kyc.KycEvaluationPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KycEvaluationHandler implements InteractionActionHandler {

    private final KycEvaluationPort evaluationPort;
    private final OnboardingOperations operations;

    public KycEvaluationHandler(KycEvaluationPort evaluationPort, OnboardingOperations operations) {
        this.evaluationPort = evaluationPort;
        this.operations = operations;
    }

    @Override
    public String action() {
        return "submit-kyc";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload,
            List.of("documentType", "documentNumber", "issuingCountry", "documentId"), List.of(), List.of());
        UUID documentId = parseDocumentId(payload.path("documentId").asText());
        boolean documentBelongsToCase = operations.findFileSets(caseId).stream()
            .flatMap(fileSet -> operations.findDocuments(fileSet.id()).stream())
            .anyMatch(document -> document.id().equals(documentId)
                && document.objectKey().startsWith("content-service:")
                && "AVAILABLE".equals(document.status()));
        if (!documentBelongsToCase) {
            throw new InvalidActionPayloadException(
                "Field 'documentId' must reference an available document in this onboarding case");
        }
        KycEvaluationPort.EvaluationResult result = evaluationPort.evaluate(caseId, payload);
        currentData.set("kyc", payload.deepCopy());
        currentData.putObject("kycEvaluation")
            .put("provider", result.provider())
            .put("status", result.status().name())
            .put("reference", result.reference());
        return ActionOutcome.success(currentData);
    }

    private UUID parseDocumentId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidActionPayloadException("Field 'documentId' must be a valid UUID");
        }
    }
}
