package com.v52alex.onboarding.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.domain.AuditEventOutbox;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcAuditEventOutbox implements AuditEventOutbox {

    private static final String EVENT_NAME = "onboarding.case-event.v1";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    JdbcAuditEventOutbox(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public void enqueue(UUID aggregateId, String eventType, String actorId, Instant occurredAt,
                        Map<String, Object> payload) {
        UUID eventId = UUID.randomUUID();
        jdbc.update("""
            insert into onboarding_outbox_event
              (id, aggregate_id, event_type, payload, status, attempts, created_at, next_attempt_at)
            values (?, ?, ?, ?, 'PENDING', 0, ?, ?)
            """, eventId.toString(), aggregateId.toString(), eventType,
            serialize(eventId, aggregateId, eventType, actorId, occurredAt, payload),
            Timestamp.from(occurredAt), Timestamp.from(occurredAt));
    }

    private String serialize(UUID eventId, UUID aggregateId, String eventType, String actorId,
                             Instant occurredAt, Map<String, Object> payload) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", eventId);
        envelope.put("eventName", EVENT_NAME);
        envelope.put("eventType", eventType);
        envelope.put("aggregateType", "ONBOARDING_CASE");
        envelope.put("aggregateId", aggregateId);
        envelope.put("actorId", actorId == null ? "" : actorId);
        envelope.put("correlationId", "");
        envelope.put("occurredAt", occurredAt.toString());
        envelope.put("payload", payload);
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Audit outbox event cannot be serialized", exception);
        }
    }
}
