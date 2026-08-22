package com.v52alex.onboarding.infrastructure.messaging;

import java.util.UUID;

interface EventMessagePublisher {
    void publish(UUID eventId, String payload);
}
