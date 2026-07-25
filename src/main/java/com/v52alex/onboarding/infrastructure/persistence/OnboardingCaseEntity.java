package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.domain.OnboardingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "onboarding_case")
class OnboardingCaseEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 100)
    String workflowKey;

    @Column(nullable = false, length = 100)
    String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    OnboardingStatus status;

    @Column(nullable = false, columnDefinition = "text")
    String data;

    @Version
    long version;

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @Column(nullable = false)
    Instant updatedAt;

    protected OnboardingCaseEntity() {
    }
}

