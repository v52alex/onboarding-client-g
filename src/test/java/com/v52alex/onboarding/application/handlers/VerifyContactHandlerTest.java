package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.integration.otp.DummyOtpVerificationAdapter;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VerifyContactHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VerifyContactHandler handler = new VerifyContactHandler(new DummyOtpVerificationAdapter());

    @Test
    void verifiesTheDevelopmentCodeAgainstTheRequestedChallenge() {
        UUID caseId = UUID.randomUUID();
        ObjectNode data = challenge(caseId);

        var result = handler.handle(caseId, data,
            objectMapper.createObjectNode().put("verificationCode", "123456"));

        assertThat(result.updatedData().at("/contactVerification/status").asText())
            .isEqualTo("VERIFIED");
    }

    @Test
    void rejectsAnInvalidCodeWithoutAdvancing() {
        UUID caseId = UUID.randomUUID();

        assertThatThrownBy(() -> handler.handle(caseId, challenge(caseId),
            objectMapper.createObjectNode().put("verificationCode", "000000")))
            .isInstanceOf(InvalidActionPayloadException.class)
            .hasMessageContaining("invalid or expired");
    }

    private ObjectNode challenge(UUID caseId) {
        ObjectNode data = objectMapper.createObjectNode();
        data.putObject("contactChallenge")
            .put("channel", "EMAIL")
            .put("destination", "alexis@example.test")
            .put("challengeId", "dummy-otp-" + caseId);
        return data;
    }
}
