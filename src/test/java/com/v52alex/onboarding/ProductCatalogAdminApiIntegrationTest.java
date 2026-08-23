package com.v52alex.onboarding;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductCatalogAdminApiIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void protectsCatalogAdministrationByPlatformAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/onboarding-products"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/onboarding-products")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_onboarding-manager"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void createsUpdatesAndDeactivatesAProduct() throws Exception {
        String id = "term-deposit-test";
        mockMvc.perform(post("/api/v1/admin/onboarding-products")
                .with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"id":"%s","code":"TERM_DEPOSIT_TEST","name":"Term deposit test",
                     "description":"Test product","active":true,"displayOrder":30}
                    """.formatted(id)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(put("/api/v1/admin/onboarding-products/{id}", id)
                .with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"TERM_DEPOSIT_TEST","name":"Updated term deposit",
                     "description":"Updated description","active":true,"displayOrder":5}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated term deposit"))
            .andExpect(jsonPath("$.displayOrder").value(5));

        mockMvc.perform(patch("/api/v1/admin/onboarding-products/{id}/status", id)
                .with(admin()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return jwt().jwt(token -> token.subject("platform-admin-id")
                .claim("preferred_username", "platform.admin"))
            .authorities(new SimpleGrantedAuthority("ROLE_platform-admin"));
    }
}
