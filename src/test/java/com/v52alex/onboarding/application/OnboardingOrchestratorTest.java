package com.v52alex.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingCaseRepository;
import com.v52alex.onboarding.domain.OnboardingStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class OnboardingOrchestratorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OnboardingOrchestrator orchestrator;

    @BeforeEach
    void setUp() throws Exception {
        ByteArrayResource workflow = new ByteArrayResource("""
            {
              "key": "test",
              "version": 1,
              "initialStep": "collect",
              "steps": [
                {
                  "name": "collect",
                  "action": "submit",
                  "terminal": false,
                  "transitions": {"success": "completed"}
                },
                {
                  "name": "completed",
                  "action": null,
                  "terminal": true,
                  "transitions": {}
                }
              ]
            }
            """.getBytes());
        WorkflowCatalog catalog = new WorkflowCatalog(objectMapper, new ByteArrayResource[]{workflow});
        catalog.load();
        orchestrator = new OnboardingOrchestrator(
            new InMemoryRepository(),
            catalog,
            objectMapper,
            java.util.List.of(new TestHandler())
        );
    }

    @Test
    void startsAtTheConfiguredInitialStep() {
        OnboardingCase result = orchestrator.start("test");

        assertThat(result.currentStep()).isEqualTo("collect");
        assertThat(result.status()).isEqualTo(OnboardingStatus.IN_PROGRESS);
        assertThat(result.version()).isZero();
    }

    @Test
    void executesTheCurrentActionAndCompletesTheCase() {
        OnboardingCase started = orchestrator.start("test");

        OnboardingCase result = orchestrator.execute(
            started.id(),
            "submit",
            objectMapper.createObjectNode().put("value", "accepted")
        );

        assertThat(result.currentStep()).isEqualTo("completed");
        assertThat(result.status()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(result.data()).contains("\"value\":\"accepted\"");
    }

    @Test
    void rejectsAnActionThatDoesNotBelongToTheCurrentStep() {
        OnboardingCase started = orchestrator.start("test");

        assertThatThrownBy(() ->
            orchestrator.execute(started.id(), "unexpected", objectMapper.createObjectNode()))
            .isInstanceOf(InvalidTransitionException.class)
            .hasMessageContaining("expected 'submit'");
    }

    @Test
    void rejectsActionsAfterTheCaseHasCompleted() {
        OnboardingCase started = orchestrator.start("test");
        OnboardingCase completed = orchestrator.execute(
            started.id(), "submit", objectMapper.createObjectNode());

        assertThatThrownBy(() ->
            orchestrator.execute(completed.id(), "submit", objectMapper.createObjectNode()))
            .isInstanceOf(InvalidTransitionException.class)
            .hasMessageContaining("already COMPLETED");
    }

    @Test
    void reportsUnknownCaseIds() {
        assertThatThrownBy(() -> orchestrator.get(UUID.randomUUID()))
            .isInstanceOf(CaseNotFoundException.class);
    }

    private static final class TestHandler implements InteractionActionHandler {
        @Override
        public String action() {
            return "submit";
        }

        @Override
        public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
            currentData.set("submission", payload);
            return ActionOutcome.success(currentData);
        }
    }

    private static final class InMemoryRepository implements OnboardingCaseRepository {
        private final Map<UUID, OnboardingCase> cases = new HashMap<>();

        @Override
        public OnboardingCase save(OnboardingCase onboardingCase) {
            OnboardingCase previous = cases.get(onboardingCase.id());
            long version = previous == null ? 0 : previous.version() + 1;
            OnboardingCase saved = new OnboardingCase(
                onboardingCase.id(),
                onboardingCase.workflowKey(),
                onboardingCase.currentStep(),
                onboardingCase.status(),
                onboardingCase.data(),
                version,
                onboardingCase.createdAt(),
                onboardingCase.updatedAt()
            );
            cases.put(saved.id(), saved);
            return saved;
        }

        @Override
        public Optional<OnboardingCase> findById(UUID id) {
            return Optional.ofNullable(cases.get(id));
        }
    }
}

