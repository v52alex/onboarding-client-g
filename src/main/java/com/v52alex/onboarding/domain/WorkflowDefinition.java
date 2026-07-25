package com.v52alex.onboarding.domain;

import java.util.List;
import java.util.Map;

public record WorkflowDefinition(
    String key,
    int version,
    String initialStep,
    List<StepDefinition> steps
) {
    public StepDefinition requireStep(String name) {
        return steps.stream()
            .filter(step -> step.name().equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unknown workflow step: " + name));
    }

    public record StepDefinition(
        String name,
        String action,
        boolean terminal,
        Map<String, String> transitions
    ) {
        public String nextStep(String outcome) {
            String target = transitions.get(outcome);
            if (target == null) {
                throw new IllegalStateException(
                    "No transition for outcome '%s' from step '%s'".formatted(outcome, name));
            }
            return target;
        }
    }
}

