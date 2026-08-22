package com.v52alex.onboarding.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventStore {
    List<OutboxEvent> findPublishable(Instant now, int batchSize);
    boolean claim(UUID eventId, Instant now, Instant staleBefore);
    void markPublished(UUID eventId, Instant publishedAt);
    void markFailed(UUID eventId, Instant nextAttemptAt, String error);
}
