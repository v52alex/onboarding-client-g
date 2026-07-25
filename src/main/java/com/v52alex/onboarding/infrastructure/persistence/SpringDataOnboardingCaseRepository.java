package com.v52alex.onboarding.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOnboardingCaseRepository extends JpaRepository<OnboardingCaseEntity, UUID> {
}

