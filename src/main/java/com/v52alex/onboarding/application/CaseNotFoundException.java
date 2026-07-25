package com.v52alex.onboarding.application;

import java.util.UUID;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(UUID caseId) {
        super("Onboarding case not found: " + caseId);
    }
}

