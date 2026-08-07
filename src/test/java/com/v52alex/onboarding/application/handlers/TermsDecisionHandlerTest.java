package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TermsDecisionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TermsDecisionHandler handler = new TermsDecisionHandler();

    @Test
    void continuesWhenTermsAreAccepted() {
        var result = handler.handle(UUID.randomUUID(), objectMapper.createObjectNode(),
            objectMapper.createObjectNode().put("documentVersion", "terms-v1").put("accepted", true));

        assertThat(result.code()).isEqualTo("success");
        assertThat(result.updatedData().at("/consent/accepted").asBoolean()).isTrue();
    }

    @Test
    void rejectsWhenTermsAreNotAccepted() {
        var result = handler.handle(UUID.randomUUID(), objectMapper.createObjectNode(),
            objectMapper.createObjectNode().put("documentVersion", "terms-v1").put("accepted", false));

        assertThat(result.code()).isEqualTo("rejected");
        assertThat(result.updatedData().at("/consent/accepted").asBoolean()).isFalse();
    }
}
