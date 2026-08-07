package com.v52alex.onboarding;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CaseManagementApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    private UUID caseId;

    @BeforeEach
    void createCompletedCase() {
        caseId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbc.update("""
            insert into onboarding_case
                (id, workflow_key, current_step, status, data, version, created_at, updated_at)
            values (?, 'onboarding', 'completed', 'COMPLETED', '{}', 10, ?, ?)
            """, caseId.toString(), Timestamp.from(now), Timestamp.from(now));
        jdbc.update("""
            insert into onboarding_case_review
                (case_id, review_status, created_at, updated_at)
            values (?, 'PENDING', ?, ?)
            """, caseId.toString(), Timestamp.from(now), Timestamp.from(now));
    }

    @Test
    void protectsTheOperationalQueueByRole() throws Exception {
        mockMvc.perform(get("/api/v1/case-management/cases"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/case-management/cases")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_prospect"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void listsAssignsAndDecidesACaseWithAuditEvents() throws Exception {
        mockMvc.perform(get("/api/v1/case-management/cases")
                .param("status", "PENDING")
                .with(operator()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[?(@.id == '%s')]", caseId).exists());

        mockMvc.perform(post("/api/v1/case-management/cases/{caseId}/assignment", caseId)
                .with(operator()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.review.assignedTo").value("case.manager"));

        mockMvc.perform(post("/api/v1/case-management/cases/{caseId}/decision", caseId)
                .with(operator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"REJECTED\",\"reason\":\"Document is unreadable\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.review.status").value("REJECTED"))
            .andExpect(jsonPath("$.review.decidedBy").value("case.manager"))
            .andExpect(jsonPath("$.reviewEvents.length()").value(2));
    }

    @Test
    void requiresAssignmentAndAReasonBeforeRejecting() throws Exception {
        mockMvc.perform(post("/api/v1/case-management/cases/{caseId}/decision", caseId)
                .with(operator())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"decision\":\"REJECTED\",\"reason\":\"\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("A rejection reason is required"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor operator() {
        return jwt().jwt(token -> token.subject("operator-id")
                .claim("preferred_username", "case.manager"))
            .authorities(new SimpleGrantedAuthority("ROLE_case-manager"));
    }
}
