package com.v52alex.onboarding.infrastructure.product;

import com.v52alex.onboarding.domain.OnboardingProduct;
import com.v52alex.onboarding.domain.ProductCatalog;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Local catalog used until a dedicated Product Service is integrated.
 */
@Component
class StaticProductCatalog implements ProductCatalog {

    private static final List<OnboardingProduct> PRODUCTS = List.of(
        new OnboardingProduct("checking-account", "CHECKING_ACCOUNT", "Checking account",
            "An everyday account for payments and transfers."),
        new OnboardingProduct("savings-account", "SAVINGS_ACCOUNT", "Savings account",
            "An account designed to save and earn interest.")
    );

    @Override
    public List<OnboardingProduct> availableProducts() {
        return PRODUCTS;
    }
}
