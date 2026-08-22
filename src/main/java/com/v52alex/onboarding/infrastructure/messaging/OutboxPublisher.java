package com.v52alex.onboarding.infrastructure.messaging;

import com.v52alex.onboarding.application.OutboxEvent;
import com.v52alex.onboarding.application.OutboxEventStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "onboarding.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
class OutboxPublisher {
    static final String EXCHANGE = "onboarding.events";
    static final String ROUTING_KEY = "onboarding.case-event.v1";
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventStore store;
    private final EventMessagePublisher eventPublisher;
    private final OutboxPayloadMapper payloadMapper;
    private final Clock clock;
    private final int batchSize;

    OutboxPublisher(OutboxEventStore store, EventMessagePublisher eventPublisher, OutboxPayloadMapper payloadMapper,
                    Clock clock,
                    @Value("${onboarding.outbox.batch-size:20}") int batchSize) {
        this.store = store;
        this.eventPublisher = eventPublisher;
        this.payloadMapper = payloadMapper;
        this.clock = clock;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${onboarding.outbox.fixed-delay:5000}")
    public void publishPendingEvents() {
        Instant now = clock.instant();
        List<OutboxEvent> events = store.findPublishable(now, batchSize);
        for (OutboxEvent event : events) {
            if (!store.claim(event.id(), now, now.minus(Duration.ofMinutes(5)))) continue;
            try {
                eventPublisher.publish(event.id(), payloadMapper.toAuditContract(event));
                store.markPublished(event.id(), clock.instant());
                log.info("outbox_event_published eventId={} aggregateId={} eventType={}",
                    event.id(), event.aggregateId(), event.eventType());
            } catch (RuntimeException exception) {
                Instant nextAttempt = clock.instant().plus(retryDelay(event.attempts() + 1));
                store.markFailed(event.id(), nextAttempt, exception.getMessage() == null
                    ? exception.getClass().getSimpleName() : exception.getMessage());
                log.warn("outbox_event_publish_failed eventId={} attempt={}", event.id(),
                    event.attempts() + 1, exception);
            }
        }
    }

    private Duration retryDelay(int attempts) {
        return Duration.ofSeconds(Math.min(300, 1L << Math.min(8, attempts)));
    }
}
