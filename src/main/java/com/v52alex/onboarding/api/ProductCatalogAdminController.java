package com.v52alex.onboarding.api;

import com.v52alex.onboarding.application.ProductCatalogAdminService;
import com.v52alex.onboarding.application.ProductCatalogAdminService.ProductRequest;
import com.v52alex.onboarding.domain.ManagedProduct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/onboarding-products")
public class ProductCatalogAdminController {

    private final ProductCatalogAdminService service;

    public ProductCatalogAdminController(ProductCatalogAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<ManagedProduct> list() {
        return service.allProducts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagedProduct create(@Valid @RequestBody ProductRequestBody request) {
        return service.create(request.toServiceRequest());
    }

    @PutMapping("/{id}")
    public ManagedProduct update(@PathVariable String id, @Valid @RequestBody ProductUpdateBody request) {
        return service.update(id, request.toServiceRequest(id));
    }

    @PatchMapping("/{id}/status")
    public ManagedProduct updateStatus(@PathVariable String id, @Valid @RequestBody StatusBody request) {
        return service.updateStatus(id, request.active());
    }

    public record ProductRequestBody(
        @NotBlank @Pattern(regexp = "[a-z0-9-]+") @Size(max = 100) String id,
        @NotBlank @Pattern(regexp = "[A-Z0-9_]+") @Size(max = 100) String code,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 500) String description,
        boolean active,
        @Min(0) int displayOrder
    ) {
        ProductRequest toServiceRequest() {
            return new ProductRequest(id, code, name, description, active, displayOrder);
        }
    }

    public record ProductUpdateBody(
        @NotBlank @Pattern(regexp = "[A-Z0-9_]+") @Size(max = 100) String code,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 500) String description,
        boolean active,
        @Min(0) int displayOrder
    ) {
        ProductRequest toServiceRequest(String id) {
            return new ProductRequest(id, code, name, description, active, displayOrder);
        }
    }

    public record StatusBody(boolean active) {
    }
}
