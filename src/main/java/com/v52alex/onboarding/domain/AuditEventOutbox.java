package com.v52alex.onboarding.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Transactional boundary for events that must be propagated to the audit capability.
 */
public interface AuditEventOutbox {

    void enqueue(UUID aggregateId, String eventType, String actorId, Instant occurredAt,
                 Map<String, Object> payload);
}
