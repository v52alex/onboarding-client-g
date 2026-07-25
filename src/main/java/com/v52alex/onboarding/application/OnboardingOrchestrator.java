package com.v52alex.onboarding.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingCaseRepository;
import com.v52alex.onboarding.domain.OnboardingStatus;
import com.v52alex.onboarding.domain.WorkflowDefinition;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Service
public class OnboardingOrchestrator {

    private final OnboardingCaseRepository repository;
    private final WorkflowCatalog catalog;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Map<String, InteractionActionHandler> handlers;

    public OnboardingOrchestrator(
        OnboardingCaseRepository repository,
        WorkflowCatalog catalog,
        ObjectMapper objectMapper,
        List<InteractionActionHandler> handlers
    ) {
        this.repository = repository;
        this.catalog = catalog;
        this.objectMapper = objectMapper;
        this.clock = Clock.systemUTC();
        this.handlers = handlers.stream()
            .collect(Collectors.toUnmodifiableMap(InteractionActionHandler::action, Function.identity()));
    }

    @Transactional
    public OnboardingCase start(String workflowKey) {
        WorkflowDefinition workflow = catalog.require(workflowKey);
        Instant now = clock.instant();
        return repository.save(new OnboardingCase(
            UUID.randomUUID(),
            workflow.key(),
            workflow.initialStep(),
            OnboardingStatus.IN_PROGRESS,
            "{}",
            0,
            now,
            now
        ));
    }

    @Transactional(readOnly = true)
    public OnboardingCase get(UUID caseId) {
        return repository.findById(caseId)
            .orElseThrow(() -> new CaseNotFoundException(caseId));
    }

    @Transactional
    public OnboardingCase execute(UUID caseId, String action, JsonNode payload) {
        OnboardingCase current = get(caseId);
        if (current.status() != OnboardingStatus.IN_PROGRESS) {
            throw new InvalidTransitionException("Onboarding case is already " + current.status());
        }

        WorkflowDefinition workflow = catalog.require(current.workflowKey());
        WorkflowDefinition.StepDefinition step = workflow.requireStep(current.currentStep());
        if (!action.equals(step.action())) {
            throw new InvalidTransitionException(
                "Action '%s' is not allowed at step '%s'; expected '%s'"
                    .formatted(action, step.name(), step.action()));
        }

        InteractionActionHandler handler = handlers.get(action);
        if (handler == null) {
            throw new IllegalStateException("No handler registered for action: " + action);
        }

        ObjectNode currentData = readData(current.data());
        ActionOutcome outcome = handler.handle(caseId, currentData, payload);
        String nextStep = step.nextStep(outcome.code());
        WorkflowDefinition.StepDefinition next = workflow.requireStep(nextStep);
        OnboardingStatus status = statusFor(next);

        return repository.save(new OnboardingCase(
            current.id(),
            current.workflowKey(),
            nextStep,
            status,
            writeData(outcome.updatedData()),
            current.version(),
            current.createdAt(),
            clock.instant()
        ));
    }

    public WorkflowDefinition.StepDefinition currentInteraction(OnboardingCase onboardingCase) {
        return catalog.require(onboardingCase.workflowKey()).requireStep(onboardingCase.currentStep());
    }

    private OnboardingStatus statusFor(WorkflowDefinition.StepDefinition next) {
        if (!next.terminal()) {
            return OnboardingStatus.IN_PROGRESS;
        }
        return "declined".equals(next.name()) ? OnboardingStatus.DECLINED : OnboardingStatus.COMPLETED;
    }

    private ObjectNode readData(String data) {
        try {
            return (ObjectNode) objectMapper.readTree(data);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored case data is invalid", exception);
        }
    }

    private String writeData(ObjectNode data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Case data cannot be serialized", exception);
        }
    }
}

