package com.v52alex.onboarding.application.handlers;

import com.v52alex.onboarding.application.InteractionActionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InteractionHandlersConfiguration {

    @Bean
    InteractionActionHandler selectProductsHandler() {
        return new JsonSectionActionHandler("select-products", "productSelection");
    }

    @Bean
    InteractionActionHandler acceptTermsHandler() {
        return new JsonSectionActionHandler("accept-terms", "consent");
    }

    @Bean
    InteractionActionHandler submitPersonalDataHandler() {
        return new JsonSectionActionHandler("submit-personal-data", "applicant");
    }

    @Bean
    InteractionActionHandler submitKycHandler() {
        return new JsonSectionActionHandler("submit-kyc", "kyc");
    }

    @Bean
    InteractionActionHandler submitAddressHandler() {
        return new JsonSectionActionHandler("submit-address", "address");
    }

    @Bean
    InteractionActionHandler verifyContactHandler() {
        return new JsonSectionActionHandler("verify-contact", "contactVerification");
    }

    @Bean
    InteractionActionHandler submitEnrollmentHandler() {
        return new JsonSectionActionHandler("submit-enrollment", "enrollment");
    }

    @Bean
    InteractionActionHandler acceptContractHandler() {
        return new JsonSectionActionHandler("accept-contract", "contract");
    }
}

