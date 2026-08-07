package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.integration.otp.DummyOtpVerificationAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RequestOtpHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestOtpHandler handler = new RequestOtpHandler(new DummyOtpVerificationAdapter());

    @Test
    void createsAChallengeForAValidEmailDestination() {
        UUID caseId = UUID.randomUUID();

        var result = handler.handle(caseId, objectMapper.createObjectNode(),
            objectMapper.createObjectNode().put("channel", "EMAIL")
                .put("destination", "alexis@example.test"));

        assertThat(result.updatedData().at("/contactChallenge/destination").asText())
            .isEqualTo("alexis@example.test");
        assertThat(result.updatedData().at("/contactChallenge/challengeId").asText())
            .isEqualTo("dummy-otp-" + caseId);
    }

    @Test
    void rejectsADestinationThatDoesNotMatchItsChannel() {
        assertThatThrownBy(() -> handler.handle(UUID.randomUUID(), objectMapper.createObjectNode(),
            objectMapper.createObjectNode().put("channel", "PHONE")
                .put("destination", "not-a-phone")))
            .isInstanceOf(InvalidActionPayloadException.class)
            .hasMessageContaining("valid EMAIL or PHONE");
    }
}
