package com.v52alex.onboarding.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.application.OnboardingOrchestrator;
import com.v52alex.onboarding.application.ActionContext;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.domain.OperationalRecords.CaseEvent;
import com.v52alex.onboarding.domain.OperationalRecords.Consent;
import com.v52alex.onboarding.domain.WorkflowDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@RestController
@RequestMapping("/api/v1/onboarding-cases")
@Validated
public class OnboardingController {

    private final OnboardingOrchestrator orchestrator;
    private final ObjectMapper objectMapper;
    private final OnboardingOperations operations;

    public OnboardingController(OnboardingOrchestrator orchestrator, ObjectMapper objectMapper,
        OnboardingOperations operations) {
        this.orchestrator = orchestrator;
        this.objectMapper = objectMapper;
        this.operations = operations;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaseResponse start(@Valid @RequestBody StartCaseRequest request) {
        return toResponse(orchestrator.start(request.workflowKey()));
    }

    @GetMapping("/{caseId}")
    public CaseResponse get(@PathVariable UUID caseId) {
        return toResponse(orchestrator.get(caseId));
    }

    @GetMapping("/{caseId}/interaction")
    public InteractionResponse interaction(@PathVariable UUID caseId) {
        OnboardingCase onboardingCase = orchestrator.get(caseId);
        WorkflowDefinition.StepDefinition step = orchestrator.currentInteraction(onboardingCase);
        return new InteractionResponse(
            onboardingCase.id(),
            step.name(),
            step.action(),
            step.terminal(),
            onboardingCase.status().name(),
            step.metadata()
        );
    }

    @PostMapping("/{caseId}/actions/{action}")
    public CaseResponse execute(
        @PathVariable UUID caseId,
        @PathVariable String action,
        @RequestHeader(value = "Idempotency-Key", required = false) @Size(min = 1, max = 150) String idempotencyKey,
        @RequestHeader(value = "X-Actor-Id", required = false) @Size(max = 150) String actorId,
        @RequestHeader(value = "X-Correlation-Id", required = false) @Size(max = 100) String correlationId,
        @RequestBody JsonNode payload
    ) {
        return toResponse(orchestrator.execute(caseId, action, payload,
            new ActionContext(idempotencyKey, actorId, correlationId)));
    }

    @GetMapping("/{caseId}/events")
    public List<CaseEvent> events(@PathVariable UUID caseId) {
        orchestrator.get(caseId);
        return operations.findEvents(caseId);
    }

    @GetMapping("/{caseId}/consents")
    public List<Consent> consents(@PathVariable UUID caseId) {
        orchestrator.get(caseId);
        return operations.findConsents(caseId);
    }

    private CaseResponse toResponse(OnboardingCase onboardingCase) {
        try {
            return new CaseResponse(
                onboardingCase.id(),
                onboardingCase.workflowKey(),
                onboardingCase.currentStep(),
                onboardingCase.status().name(),
                objectMapper.readTree(onboardingCase.data()),
                onboardingCase.version(),
                onboardingCase.createdAt(),
                onboardingCase.updatedAt()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Case data cannot be returned", exception);
        }
    }

    public record StartCaseRequest(@NotBlank String workflowKey) {
    }
}
