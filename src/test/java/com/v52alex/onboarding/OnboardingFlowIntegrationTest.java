package com.v52alex.onboarding;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.OnboardingOrchestrator;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OnboardingFlowIntegrationTest {

    @Autowired
    private OnboardingOrchestrator orchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesTheConfiguredOnboardingFlow() {
        OnboardingCase onboardingCase = orchestrator.start("onboarding");

        List<String> actions = List.of(
            "select-products",
            "accept-terms",
            "submit-personal-data",
            "submit-kyc",
            "submit-address",
            "verify-contact",
            "verify-identity",
            "submit-enrollment",
            "accept-contract"
        );

        for (String action : actions) {
            ObjectNode payload = objectMapper.createObjectNode().put("action", action);
            onboardingCase = orchestrator.execute(onboardingCase.id(), action, payload);
        }

        assertThat(onboardingCase.status()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(onboardingCase.currentStep()).isEqualTo("completed");
        assertThat(onboardingCase.version()).isEqualTo(actions.size());
    }
}

