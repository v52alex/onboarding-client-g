package com.v52alex.onboarding.application;

public class InvalidActionPayloadException extends RuntimeException {
    public InvalidActionPayloadException(String message) {
        super(message);
    }
}
