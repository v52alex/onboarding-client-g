package com.v52alex.onboarding.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.domain.WorkflowDefinition;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Component
public class WorkflowCatalog {

    private final ObjectMapper objectMapper;
    private final Resource[] definitions;
    private final Map<String, WorkflowDefinition> workflows = new HashMap<>();

    public WorkflowCatalog(
        ObjectMapper objectMapper,
        @Value("classpath*:interactions/*.json") Resource[] definitions
    ) {
        this.objectMapper = objectMapper;
        this.definitions = definitions;
    }

    @PostConstruct
    void load() throws IOException {
        for (Resource resource : definitions) {
            WorkflowDefinition definition =
                objectMapper.readValue(resource.getInputStream(), WorkflowDefinition.class);
            validate(definition);
            if (workflows.put(definition.key(), definition) != null) {
                throw new IllegalStateException("Duplicated workflow key: " + definition.key());
            }
        }
    }

    public WorkflowDefinition require(String key) {
        WorkflowDefinition definition = workflows.get(key);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown workflow: " + key);
        }
        return definition;
    }

    private void validate(WorkflowDefinition definition) {
        definition.requireStep(definition.initialStep());
        for (WorkflowDefinition.StepDefinition step : definition.steps()) {
            if (!step.terminal() && (step.action() == null || step.transitions().isEmpty())) {
                throw new IllegalStateException("Non-terminal step must define action and transitions: " + step.name());
            }
            for (String target : step.transitions().values()) {
                definition.requireStep(target);
            }
        }
    }
}

