package com.v52alex.onboarding.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.v52alex.onboarding.application.CaseManagementService;
import com.v52alex.onboarding.application.CaseManagementService.ManagedCase;
import com.v52alex.onboarding.domain.CaseManagementRecords.CasePage;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewDecision;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewEvent;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewStatus;
import com.v52alex.onboarding.domain.CaseManagementRecords.Review;
import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.domain.OperationalRecords.CaseEvent;
import com.v52alex.onboarding.domain.OperationalRecords.Document;
import com.v52alex.onboarding.domain.OperationalRecords.FileSet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/case-management/cases")
@Validated
public class CaseManagementController {

    private final CaseManagementService service;
    private final OnboardingOperations operations;
    private final ObjectMapper objectMapper;

    public CaseManagementController(CaseManagementService service, OnboardingOperations operations,
        ObjectMapper objectMapper) {
        this.service = service;
        this.operations = operations;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public CasePage search(
        @RequestParam(required = false) ReviewStatus status,
        @RequestParam(required = false) @Size(max = 150) String assignedTo,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return service.search(status, assignedTo, page, size);
    }

    @GetMapping("/{caseId}")
    public CaseDetail get(@PathVariable UUID caseId) {
        return detail(service.get(caseId));
    }

    @PostMapping("/{caseId}/assignment")
    public CaseDetail assign(@PathVariable UUID caseId, @AuthenticationPrincipal Jwt jwt) {
        return detail(service.assign(caseId, actor(jwt)));
    }

    @PostMapping("/{caseId}/decision")
    public CaseDetail decide(@PathVariable UUID caseId, @Valid @RequestBody DecisionRequest request,
        @AuthenticationPrincipal Jwt jwt) {
        return detail(service.decide(caseId, request.decision(), request.reason(), actor(jwt)));
    }

    private CaseDetail detail(ManagedCase managed) {
        OnboardingCase value = managed.onboardingCase();
        try {
            CaseResponse onboarding = new CaseResponse(value.id(), value.workflowKey(), value.currentStep(),
                value.status().name(), objectMapper.readTree(value.data()), value.version(),
                value.createdAt(), value.updatedAt());
            List<FileSetDetail> fileSets = operations.findFileSets(value.id()).stream()
                .map(fileSet -> new FileSetDetail(fileSet, operations.findDocuments(fileSet.id())))
                .toList();
            return new CaseDetail(onboarding, managed.review(), operations.findEvents(value.id()),
                managed.reviewEvents(), fileSets);
        } catch (Exception exception) {
            throw new IllegalStateException("Case data cannot be returned", exception);
        }
    }

    private String actor(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return username == null || username.isBlank() ? jwt.getSubject() : username;
    }

    public record DecisionRequest(@NotNull ReviewDecision decision, @Size(max = 500) String reason) {
    }

    public record FileSetDetail(FileSet fileSet, List<Document> documents) {
    }

    public record CaseDetail(
        CaseResponse onboardingCase, Review review, List<CaseEvent> onboardingEvents,
        List<ReviewEvent> reviewEvents, List<FileSetDetail> fileSets
    ) {
    }
}
