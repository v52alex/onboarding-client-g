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
import java.util.UUID;

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

    @Test
    void incompleteDataDoesNotAdvanceOrCreateAnActionEvent() throws Exception {
        String caseId = createCase();

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/select-products", caseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value(
                "Field 'productIds' must be a non-empty string array"));

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentStep").value("product-selection"))
            .andExpect(jsonPath("$.version").value(0))
            .andExpect(jsonPath("$.data").isEmpty());
        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/events", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void aDifferentCaseIdCannotAlterTheInitialFlow() throws Exception {
        String originalCaseId = createCase();

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/select-products",
                UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[\"checking-account\"]}"))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}", originalCaseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentStep").value("product-selection"))
            .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void makesActionsIdempotentAndRecordsAuditEvents() throws Exception {
        String caseId = createCase();
        String payload = "{\"productIds\":[\"checking-account\"]}";

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/select-products", caseId)
                .header("Idempotency-Key", "select-products-1")
                .header("X-Actor-Id", "integration-test")
                .header("X-Correlation-Id", "correlation-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/select-products", caseId)
                .header("Idempotency-Key", "select-products-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/events", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[1].actorId").value("integration-test"))
            .andExpect(jsonPath("$[1].correlationId").value("correlation-1"));
    }

    @Test
    void exposesTermsVersionAndDeclinesTheCaseWhenTermsAreRejected() throws Exception {
        String caseId = createCase();
        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/select-products", caseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productIds\":[\"checking-account\"]}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/interaction", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("accept-terms"))
            .andExpect(jsonPath("$.metadata.documentVersion").value("terms-v1"));

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/actions/accept-terms", caseId)
                .header("X-Actor-Id", "prospect-123")
                .header("X-Correlation-Id", "terms-rejected-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentVersion\":\"terms-v1\",\"accepted\":false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DECLINED"))
            .andExpect(jsonPath("$.currentStep").value("declined"));

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/interaction", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.terminal").value(true))
            .andExpect(jsonPath("$.status").value("DECLINED"));
        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/consents", caseId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].documentVersion").value("terms-v1"))
            .andExpect(jsonPath("$[0].accepted").value(false))
            .andExpect(jsonPath("$[0].correlationId").value("terms-rejected-1"));
    }

    @Test
    void registersDocumentMetadataInsideACaseFileSet() throws Exception {
        String caseId = createCase();
        String fileSetResponse = mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/file-sets", caseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Identity documents","maxFiles":2,
                     "allowedMediaTypes":["application/pdf","image/jpeg"]}
                    """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        String fileSetId = new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(fileSetResponse).get("id").asText();

        mockMvc.perform(post("/api/v1/onboarding-cases/{caseId}/file-sets/{fileSetId}/documents",
                caseId, fileSetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"objectKey":"cases/test/passport.pdf","originalFileName":"passport.pdf",
                     "mimeType":"application/pdf","sizeBytes":1024,
                     "checksumSha256":"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(get("/api/v1/onboarding-cases/{caseId}/file-sets/{fileSetId}/documents",
                caseId, fileSetId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].originalFileName").value("passport.pdf"));
    }

    private String createCase() throws Exception {
        String response = mockMvc.perform(post("/api/v1/onboarding-cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workflowKey\":\"onboarding\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asText();
    }
}
