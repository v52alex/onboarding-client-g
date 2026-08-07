package com.v52alex.onboarding.domain;

import com.v52alex.onboarding.domain.OperationalRecords.CachedAction;
import com.v52alex.onboarding.domain.OperationalRecords.CaseEvent;
import com.v52alex.onboarding.domain.OperationalRecords.Consent;
import com.v52alex.onboarding.domain.OperationalRecords.Document;
import com.v52alex.onboarding.domain.OperationalRecords.FileSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OnboardingOperations {

    void recordEvent(CaseEvent event, String outboxPayload);

    void recordConsent(Consent consent);

    List<CaseEvent> findEvents(UUID caseId);

    List<Consent> findConsents(UUID caseId);

    Optional<CachedAction> findCachedAction(UUID caseId, String action, String idempotencyKey);

    void saveCachedAction(UUID caseId, String action, String idempotencyKey, CachedAction cachedAction);

    FileSet createFileSet(FileSet fileSet);

    List<FileSet> findFileSets(UUID caseId);

    Optional<FileSet> findFileSet(UUID fileSetId);

    Document addDocument(Document document);

    List<Document> findDocuments(UUID fileSetId);
}
