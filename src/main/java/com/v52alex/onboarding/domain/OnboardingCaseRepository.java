package com.v52alex.onboarding.domain;

import java.util.Optional;
import java.util.UUID;

public interface OnboardingCaseRepository {

    OnboardingCase save(OnboardingCase onboardingCase);

    Optional<OnboardingCase> findById(UUID id);
}

