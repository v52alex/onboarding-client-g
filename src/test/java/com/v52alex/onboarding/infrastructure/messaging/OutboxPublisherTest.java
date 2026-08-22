package com.v52alex.onboarding.infrastructure.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.v52alex.onboarding.application.OutboxEvent;
import com.v52alex.onboarding.application.OutboxEventStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OutboxPublisherTest {
    private final OutboxEventStore store = Mockito.mock(OutboxEventStore.class);
    private final EventMessagePublisher eventPublisher = Mockito.mock(EventMessagePublisher.class);
    private final OutboxPayloadMapper payloadMapper = Mockito.mock(OutboxPayloadMapper.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-22T12:00:00Z"), ZoneOffset.UTC);
    private final OutboxPublisher publisher = new OutboxPublisher(store, eventPublisher, payloadMapper, clock, 20);

    @Test
    void publishesClaimedEventAndMarksItPublished() {
        OutboxEvent event = event();
        when(store.findPublishable(any(), eq(20))).thenReturn(List.of(event));
        when(store.claim(eq(event.id()), any(), any())).thenReturn(true);
        when(payloadMapper.toAuditContract(event)).thenReturn(event.payload());

        publisher.publishPendingEvents();

        verify(eventPublisher).publish(event.id(), event.payload());
        verify(store).markPublished(event.id(), clock.instant());
    }

    @Test
    void schedulesRetryWhenBrokerPublishingFails() {
        OutboxEvent event = event();
        when(store.findPublishable(any(), eq(20))).thenReturn(List.of(event));
        when(store.claim(eq(event.id()), any(), any())).thenReturn(true);
        when(payloadMapper.toAuditContract(event)).thenReturn(event.payload());
        Mockito.doThrow(new IllegalStateException("broker unavailable"))
            .when(eventPublisher).publish(event.id(), event.payload());

        publisher.publishPendingEvents();

        verify(store).markFailed(eq(event.id()), eq(clock.instant().plusSeconds(2)), eq("broker unavailable"));
    }

    private OutboxEvent event() {
        return new OutboxEvent(UUID.randomUUID(), "case-1", "ACTION_COMPLETED", "{\"eventId\":\"id\"}",
            0, clock.instant());
    }
}
