package com.v52alex.onboarding.application;

public class InvalidReviewTransitionException extends RuntimeException {
    public InvalidReviewTransitionException(String message) {
        super(message);
    }
}
