package com.v52alex.onboarding.domain;

/** Complete product representation used by catalog administrators. */
public record ManagedProduct(
    String id,
    String code,
    String name,
    String description,
    boolean active,
    int displayOrder
) {
}
