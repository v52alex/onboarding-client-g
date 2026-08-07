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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Entity
@Table(name = "onboarding_case")
class OnboardingCaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
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
