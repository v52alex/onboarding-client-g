package com.v52alex.onboarding.infrastructure.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OnboardingEventExchangeConfiguration {
    @Bean
    TopicExchange onboardingEventsExchange() {
        return new TopicExchange(OutboxPublisher.EXCHANGE, true, false);
    }
}
