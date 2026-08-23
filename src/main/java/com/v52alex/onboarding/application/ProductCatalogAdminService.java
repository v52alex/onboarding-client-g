package com.v52alex.onboarding.application;

import com.v52alex.onboarding.domain.ManagedProduct;
import com.v52alex.onboarding.domain.ProductCatalogAdmin;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCatalogAdminService {

    private final ProductCatalogAdmin catalog;

    public ProductCatalogAdminService(ProductCatalogAdmin catalog) {
        this.catalog = catalog;
    }

    @Transactional(readOnly = true)
    public List<ManagedProduct> allProducts() {
        return catalog.allProducts();
    }

    @Transactional
    public ManagedProduct create(ProductRequest request) {
        return catalog.create(request.id(), request.code(), request.name(), request.description(),
            request.active(), request.displayOrder());
    }

    @Transactional
    public ManagedProduct update(String id, ProductRequest request) {
        return catalog.update(id, request.code(), request.name(), request.description(),
            request.active(), request.displayOrder());
    }

    @Transactional
    public ManagedProduct updateStatus(String id, boolean active) {
        return catalog.updateStatus(id, active);
    }

    public record ProductRequest(String id, String code, String name, String description,
        boolean active, int displayOrder) {
    }
}
