package com.v52alex.onboarding.domain;

import java.util.List;

/** Administrative port for maintaining the onboarding product catalog. */
public interface ProductCatalogAdmin {

    List<ManagedProduct> allProducts();

    ManagedProduct create(String id, String code, String name, String description,
        boolean active, int displayOrder);

    ManagedProduct update(String id, String code, String name, String description,
        boolean active, int displayOrder);

    ManagedProduct updateStatus(String id, boolean active);
}
