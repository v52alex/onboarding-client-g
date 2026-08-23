package com.v52alex.onboarding.application;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("Product '" + id + "' was not found");
    }
}
