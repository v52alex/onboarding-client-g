package com.v52alex.onboarding.infrastructure.messaging;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
class OutboxSchedulingConfiguration {
    @Bean Clock clock() { return Clock.systemUTC(); }
}
