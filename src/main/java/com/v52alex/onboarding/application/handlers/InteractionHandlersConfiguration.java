package com.v52alex.onboarding.application.handlers;

import com.v52alex.onboarding.application.InteractionActionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@Configuration
public class InteractionHandlersConfiguration {

    @Bean
    InteractionActionHandler selectProductsHandler() {
        return handler("select-products", "productSelection", List.of(), List.of(), List.of("productIds"));
    }

    @Bean
    InteractionActionHandler acceptTermsHandler() {
        return new TermsDecisionHandler();
    }

    @Bean
    InteractionActionHandler submitPersonalDataHandler() {
        return handler("submit-personal-data", "applicant",
            List.of("firstName", "lastName", "dateOfBirth"), List.of(), List.of());
    }

    @Bean
    InteractionActionHandler submitAddressHandler() {
        return handler("submit-address", "address",
            List.of("line1", "city", "country"), List.of(), List.of());
    }

    @Bean
    InteractionActionHandler submitEnrollmentHandler() {
        return handler("submit-enrollment", "enrollment",
            List.of("accountType", "currency"), List.of(), List.of());
    }

    @Bean
    InteractionActionHandler acceptContractHandler() {
        return handler("accept-contract", "contract",
            List.of("contractVersion"), List.of("accepted"), List.of());
    }

    private InteractionActionHandler handler(String action, String section, List<String> requiredText,
        List<String> requiredTrue, List<String> requiredTextArrays) {
        return new JsonSectionActionHandler(action, section, requiredText, requiredTrue,
            requiredTextArrays);
    }
}
