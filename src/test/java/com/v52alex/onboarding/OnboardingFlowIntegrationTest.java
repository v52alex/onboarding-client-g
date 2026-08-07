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

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@SpringBootTest
class OnboardingFlowIntegrationTest {

    @Autowired
    private OnboardingOrchestrator orchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesTheConfiguredOnboardingFlow() throws Exception {
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
            ObjectNode payload = validPayload(action);
            onboardingCase = orchestrator.execute(onboardingCase.id(), action, payload);
        }

        assertThat(onboardingCase.status()).isEqualTo(OnboardingStatus.COMPLETED);
        assertThat(onboardingCase.currentStep()).isEqualTo("completed");
        assertThat(onboardingCase.version()).isEqualTo(actions.size());
        var storedData = objectMapper.readTree(onboardingCase.data());
        assertThat(List.of("productSelection", "consent", "applicant", "kyc", "address",
            "contactVerification", "identity", "identityVerification", "enrollment", "contract"))
            .allMatch(storedData::has);
    }

    private ObjectNode validPayload(String action) {
        ObjectNode payload = objectMapper.createObjectNode();
        return switch (action) {
            case "select-products" -> {
                payload.putArray("productIds").add("checking-account");
                yield payload;
            }
            case "accept-terms" -> payload.put("documentVersion", "terms-v1").put("accepted", true);
            case "submit-personal-data" -> payload.put("firstName", "Alexis")
                .put("lastName", "Chavez").put("dateOfBirth", "1990-01-15");
            case "submit-kyc" -> payload.put("documentType", "PASSPORT")
                .put("documentNumber", "P-DUMMY-001").put("issuingCountry", "SV");
            case "submit-address" -> payload.put("line1", "Calle Dummy 123")
                .put("city", "San Salvador").put("country", "SV");
            case "verify-contact" -> payload.put("email", "alexis@example.test")
                .put("phone", "+50370000000").put("verificationCode", "123456");
            case "verify-identity" -> payload.put("documentReference", "document-dummy-1")
                .put("livenessReference", "liveness-dummy-1");
            case "submit-enrollment" -> payload.put("accountType", "CHECKING")
                .put("currency", "USD");
            case "accept-contract" -> payload.put("contractVersion", "contract-v1")
                .put("accepted", true);
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }
}
