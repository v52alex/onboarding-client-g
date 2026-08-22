package com.v52alex.onboarding.domain;

import java.util.List;

/** Port for the products that can be selected when onboarding starts. */
public interface ProductCatalog {

    List<OnboardingProduct> availableProducts();
}
