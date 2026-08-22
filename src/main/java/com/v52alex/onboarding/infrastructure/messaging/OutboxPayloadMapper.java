package com.v52alex.onboarding.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
class OutboxPayloadMapper {
    private final ObjectMapper objectMapper;

    OutboxPayloadMapper(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    String toAuditContract(OutboxEvent event) {
        try {
            JsonNode payload = objectMapper.readTree(event.payload());
            if (payload.hasNonNull("eventId")) return event.payload();
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("eventId", event.id().toString());
            envelope.put("eventName", OutboxPublisher.ROUTING_KEY);
            envelope.put("eventType", event.eventType());
            envelope.put("aggregateType", "ONBOARDING_CASE");
            envelope.put("aggregateId", event.aggregateId());
            envelope.put("actorId", "");
            envelope.put("correlationId", "");
            envelope.put("occurredAt", event.createdAt().toString());
            envelope.set("payload", payload);
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Outbox payload is not valid JSON", exception);
        }
    }
}
