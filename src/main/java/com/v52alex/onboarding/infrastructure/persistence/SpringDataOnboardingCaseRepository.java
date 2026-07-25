package com.v52alex.onboarding.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

interface SpringDataOnboardingCaseRepository extends JpaRepository<OnboardingCaseEntity, UUID> {
}

