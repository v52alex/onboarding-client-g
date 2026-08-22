package com.v52alex.onboarding.api;

import com.v52alex.onboarding.application.ProductCatalogHandler;
import com.v52alex.onboarding.domain.OnboardingProduct;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding-products")
public class OnboardingProductController {

    private final ProductCatalogHandler products;

    public OnboardingProductController(ProductCatalogHandler products) {
        this.products = products;
    }

    @GetMapping
    public List<OnboardingProduct> list() {
        return products.availableProducts();
    }
}
