package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingCaseRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Repository
class JpaOnboardingCaseRepository implements OnboardingCaseRepository {

    private final SpringDataOnboardingCaseRepository repository;

    JpaOnboardingCaseRepository(SpringDataOnboardingCaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public OnboardingCase save(OnboardingCase onboardingCase) {
        OnboardingCaseEntity entity = repository.findById(onboardingCase.id())
            .orElseGet(OnboardingCaseEntity::new);
        entity.id = onboardingCase.id();
        entity.workflowKey = onboardingCase.workflowKey();
        entity.currentStep = onboardingCase.currentStep();
        entity.status = onboardingCase.status();
        entity.data = onboardingCase.data();
        entity.createdAt = onboardingCase.createdAt();
        entity.updatedAt = onboardingCase.updatedAt();
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<OnboardingCase> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private OnboardingCase toDomain(OnboardingCaseEntity entity) {
        return new OnboardingCase(
            entity.id,
            entity.workflowKey,
            entity.currentStep,
            entity.status,
            entity.data,
            entity.version,
            entity.createdAt,
            entity.updatedAt
        );
    }
}
