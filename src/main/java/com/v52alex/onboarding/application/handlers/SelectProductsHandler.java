package com.v52alex.onboarding.application.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.v52alex.onboarding.application.ActionOutcome;
import com.v52alex.onboarding.application.InteractionActionHandler;
import com.v52alex.onboarding.application.InvalidActionPayloadException;
import com.v52alex.onboarding.application.ProductCatalogHandler;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SelectProductsHandler implements InteractionActionHandler {

    private final ProductCatalogHandler products;

    public SelectProductsHandler(ProductCatalogHandler products) {
        this.products = products;
    }

    @Override
    public String action() {
        return "select-products";
    }

    @Override
    public ActionOutcome handle(UUID caseId, ObjectNode currentData, JsonNode payload) {
        ActionPayloadValidator.validate(payload, java.util.List.of(), java.util.List.of(),
            java.util.List.of("productIds"));
        for (JsonNode productId : payload.path("productIds")) {
            if (!products.isAvailable(productId.asText())) {
                throw new InvalidActionPayloadException("Product '%s' is not available"
                    .formatted(productId.asText()));
            }
        }
        currentData.set("productSelection", payload.deepCopy());
        return ActionOutcome.success(currentData);
    }
}
