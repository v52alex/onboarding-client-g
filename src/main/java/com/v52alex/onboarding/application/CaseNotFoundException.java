package com.v52alex.onboarding.application;

import java.util.UUID;

public class CaseNotFoundException extends RuntimeException {
    public CaseNotFoundException(UUID caseId) {
        super("Onboarding case not found: " + caseId);
    }
}

