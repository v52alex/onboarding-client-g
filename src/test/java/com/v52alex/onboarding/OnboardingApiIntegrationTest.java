package com.v52alex.onboarding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Washington Chavez Pluas
 * @since 2026-07-25
 */
@SpringBootTest
@AutoConfigureMockMvc
class OnboardingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsCaseAndExposesCurrentInteraction() throws Exception {
        String response = mockMvc.perform(post("/api/v1/onboarding-cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"workflowKey":"onboarding"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.currentStep").value("product-selection"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String caseId = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(response)
            .get("id")
            .asText();

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/interaction", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("select-products"))
            .andExpect(jsonPath("$.terminal").value(false));
    }

    @Test
    void rejectsAnActionThatDoesNotMatchTheCurrentStep() throws Exception {
        String response = mockMvc.perform(post("/api/v1/onboarding-cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"workflowKey":"onboarding"}
                    """))
            .andReturn()
            .getResponse()
            .getContentAsString();
        String caseId = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(response)
            .get("id")
            .asText();

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/accept-contract", caseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isConflict());
    }
}

