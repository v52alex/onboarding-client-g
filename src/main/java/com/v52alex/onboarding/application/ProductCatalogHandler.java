package com.v52alex.onboarding.application;

import com.v52alex.onboarding.domain.OnboardingProduct;
import com.v52alex.onboarding.domain.ProductCatalog;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Query handler for the products offered before an onboarding case exists. */
@Service
public class ProductCatalogHandler {

    private final ProductCatalog catalog;

    public ProductCatalogHandler(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public List<OnboardingProduct> availableProducts() {
        return catalog.availableProducts();
    }

    public boolean isAvailable(String productId) {
        return catalog.availableProducts().stream().anyMatch(product -> product.id().equals(productId));
    }
}
