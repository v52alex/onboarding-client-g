package com.v52alex.onboarding.domain;

/** Public product option available for a new onboarding request. */
public record OnboardingProduct(String id, String code, String name, String description) {
}
