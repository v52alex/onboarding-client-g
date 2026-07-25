package com.v52alex.onboarding.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public interface OnboardingCaseRepository {

    OnboardingCase save(OnboardingCase onboardingCase);

    Optional<OnboardingCase> findById(UUID id);
}

