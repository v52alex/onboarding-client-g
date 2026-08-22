package com.v52alex.onboarding.application;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(UUID id, String aggregateId, String eventType, String payload,
                           int attempts, Instant createdAt) {}
