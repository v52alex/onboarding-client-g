package com.v52alex.onboarding.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingCaseRepository;
import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.domain.OnboardingStatus;
import com.v52alex.onboarding.domain.OperationalRecords.CachedAction;
import com.v52alex.onboarding.domain.OperationalRecords.CaseEvent;
import com.v52alex.onboarding.domain.OperationalRecords.Consent;
import com.v52alex.onboarding.domain.WorkflowDefinition;
import java.time.Clock;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
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
    private final OnboardingOperations operations;
    private final Clock clock;
    private final Map<String, InteractionActionHandler> handlers;

    public OnboardingOrchestrator(
        OnboardingCaseRepository repository,
        OnboardingOperations operations,
        WorkflowCatalog catalog,
        ObjectMapper objectMapper,
        List<InteractionActionHandler> handlers
    ) {
        this.repository = repository;
        this.operations = operations;
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
        OnboardingCase created = repository.save(new OnboardingCase(
            UUID.randomUUID(),
            workflow.key(),
            workflow.initialStep(),
            OnboardingStatus.IN_PROGRESS,
            "{}",
            0,
            now,
            now
        ));
        recordEvent(created, "CASE_STARTED", null, null, created.currentStep(), "started",
            ActionContext.empty(), now);
        return created;
    }

    @Transactional(readOnly = true)
    public OnboardingCase get(UUID caseId) {
        return repository.findById(caseId)
            .orElseThrow(() -> new CaseNotFoundException(caseId));
    }

    @Transactional
    public OnboardingCase execute(UUID caseId, String action, JsonNode payload) {
        return execute(caseId, action, payload, ActionContext.empty());
    }

    @Transactional
    public OnboardingCase execute(UUID caseId, String action, JsonNode payload, ActionContext context) {
        String requestHash = requestHash(action, payload);
        if (context.idempotencyKey() != null) {
            var cached = operations.findCachedAction(caseId, action, context.idempotencyKey());
            if (cached.isPresent()) {
                if (!cached.get().requestHash().equals(requestHash)) {
                    throw new InvalidTransitionException(
                        "Idempotency-Key was already used with a different request");
                }
                return cached.get().result();
            }
        }
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

        Instant now = clock.instant();
        OnboardingCase saved = repository.save(new OnboardingCase(
            current.id(),
            current.workflowKey(),
            nextStep,
            status,
            writeData(outcome.updatedData()),
            current.version(),
            current.createdAt(),
            now
        ));
        recordEvent(saved, "ACTION_COMPLETED", action, current.currentStep(), nextStep,
            outcome.code(), context, now);
        recordConsentIfApplicable(saved, action, payload, context, now);
        if (context.idempotencyKey() != null) {
            operations.saveCachedAction(caseId, action, context.idempotencyKey(),
                new CachedAction(requestHash, saved));
        }
        return saved;
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

    private void recordEvent(OnboardingCase onboardingCase, String eventType, String action,
        String previousStep, String resultingStep, String outcome, ActionContext context, Instant occurredAt) {
        CaseEvent event = new CaseEvent(UUID.randomUUID(), onboardingCase.id(), onboardingCase.version(),
            eventType, action, previousStep, resultingStep, outcome, context.actorId(),
            context.correlationId(), "{}", occurredAt);
        try {
            operations.recordEvent(event, objectMapper.writeValueAsString(Map.of(
                "caseId", onboardingCase.id(),
                "workflowKey", onboardingCase.workflowKey(),
                "eventType", eventType,
                "step", resultingStep,
                "status", onboardingCase.status().name(),
                "version", onboardingCase.version()
            )));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Outbox event cannot be serialized", exception);
        }
    }

    private void recordConsentIfApplicable(OnboardingCase onboardingCase, String action, JsonNode payload,
        ActionContext context, Instant now) {
        String type;
        String versionField;
        if ("accept-terms".equals(action)) {
            type = "TERMS_AND_CONDITIONS";
            versionField = "documentVersion";
        } else if ("accept-contract".equals(action)) {
            type = "CONTRACT";
            versionField = "contractVersion";
        } else {
            return;
        }
        String version = payload.path(versionField).asText("unspecified");
        operations.recordConsent(new Consent(UUID.randomUUID(), onboardingCase.id(), type, version,
            payload.path("accepted").asBoolean(false), context.actorId(), context.correlationId(),
            payload.toString(), now));
    }

    private String requestHash(String action, JsonNode payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                (action + ":" + payload.toString()).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
