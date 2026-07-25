package com.v52alex.onboarding.application.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

class JsonSectionActionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void storesPayloadUnderItsConfiguredSectionWithoutRemovingExistingData() {
        JsonSectionActionHandler handler =
            new JsonSectionActionHandler("submit-address", "address");
        ObjectNode current = objectMapper.createObjectNode().put("existing", true);
        ObjectNode payload = objectMapper.createObjectNode().put("city", "San Salvador");

        ActionOutcome result = handler.handle(UUID.randomUUID(), current, payload);

        assertThat(result.code()).isEqualTo("success");
        assertThat(result.updatedData().get("existing").asBoolean()).isTrue();
        assertThat(result.updatedData().at("/address/city").asText()).isEqualTo("San Salvador");
    }
}

