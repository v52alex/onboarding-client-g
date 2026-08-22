package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.domain.OnboardingCase;
import com.v52alex.onboarding.domain.OnboardingOperations;
import com.v52alex.onboarding.domain.OnboardingStatus;
import com.v52alex.onboarding.domain.OperationalRecords.CachedAction;
import com.v52alex.onboarding.domain.OperationalRecords.CaseEvent;
import com.v52alex.onboarding.domain.OperationalRecords.Consent;
import com.v52alex.onboarding.domain.OperationalRecords.Document;
import com.v52alex.onboarding.domain.OperationalRecords.FileSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcOnboardingOperations implements OnboardingOperations {

    private final JdbcTemplate jdbc;

    JdbcOnboardingOperations(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void recordEvent(CaseEvent event, String outboxPayload) {
        jdbc.update("""
            insert into onboarding_case_event
              (id, case_id, event_sequence, event_type, action_name, previous_step, resulting_step,
               outcome, actor_id, correlation_id, metadata, occurred_at)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, event.id().toString(), event.caseId().toString(), event.sequence(), event.eventType(),
            event.action(), event.previousStep(), event.resultingStep(), event.outcome(), event.actorId(),
            event.correlationId(), event.metadata(), Timestamp.from(event.occurredAt()));
        jdbc.update("""
            insert into onboarding_outbox_event
              (id, aggregate_id, event_type, payload, status, attempts, created_at, next_attempt_at)
            values (?, ?, ?, ?, 'PENDING', 0, ?, ?)
            """, event.id().toString(), event.caseId().toString(), event.eventType(), outboxPayload,
            Timestamp.from(event.occurredAt()), Timestamp.from(event.occurredAt()));
    }

    @Override
    public void recordConsent(Consent consent) {
        jdbc.update("""
            insert into onboarding_consent
              (id, case_id, consent_type, document_version, accepted, actor_id, correlation_id,
               evidence, accepted_at)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, consent.id().toString(), consent.caseId().toString(), consent.type(),
            consent.documentVersion(), consent.accepted(), consent.actorId(), consent.correlationId(),
            consent.evidence(), Timestamp.from(consent.acceptedAt()));
    }

    @Override
    public List<CaseEvent> findEvents(UUID caseId) {
        return jdbc.query("""
            select id, case_id, event_sequence, event_type, action_name, previous_step,
                   resulting_step, outcome, actor_id, correlation_id, metadata, occurred_at
              from onboarding_case_event where case_id = ? order by event_sequence
            """, (rs, row) -> new CaseEvent(
                UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("case_id")),
                rs.getLong("event_sequence"), rs.getString("event_type"), rs.getString("action_name"),
                rs.getString("previous_step"), rs.getString("resulting_step"), rs.getString("outcome"),
                rs.getString("actor_id"), rs.getString("correlation_id"), rs.getString("metadata"),
                instant(rs, "occurred_at")
            ), caseId.toString());
    }

    @Override
    public List<Consent> findConsents(UUID caseId) {
        return jdbc.query("""
            select id, case_id, consent_type, document_version, accepted, actor_id,
                   correlation_id, evidence, accepted_at
              from onboarding_consent where case_id = ? order by accepted_at
            """, (rs, row) -> new Consent(
                UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("case_id")),
                rs.getString("consent_type"), rs.getString("document_version"), rs.getBoolean("accepted"),
                rs.getString("actor_id"), rs.getString("correlation_id"), rs.getString("evidence"),
                instant(rs, "accepted_at")
            ), caseId.toString());
    }

    @Override
    public Optional<CachedAction> findCachedAction(UUID caseId, String action, String idempotencyKey) {
        List<CachedAction> results = jdbc.query("""
            select request_hash, result_workflow_key, result_step, result_status, result_data, result_version,
                   result_created_at, result_updated_at
              from onboarding_idempotency_record
             where case_id = ? and action_name = ? and idempotency_key = ?
            """, (rs, row) -> new CachedAction(rs.getString("request_hash"), new OnboardingCase(
                caseId, rs.getString("result_workflow_key"), rs.getString("result_step"),
                OnboardingStatus.valueOf(rs.getString("result_status")), rs.getString("result_data"),
                rs.getLong("result_version"), instant(rs, "result_created_at"),
                instant(rs, "result_updated_at")
            )), caseId.toString(), action, idempotencyKey);
        return results.stream().findFirst();
    }

    @Override
    public void saveCachedAction(UUID caseId, String action, String idempotencyKey, CachedAction cached) {
        OnboardingCase result = cached.result();
        jdbc.update("""
            insert into onboarding_idempotency_record
              (id, case_id, action_name, idempotency_key, request_hash, result_workflow_key,
               result_step, result_status,
               result_data, result_version, result_created_at, result_updated_at, created_at)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID().toString(), caseId.toString(), action, idempotencyKey,
            cached.requestHash(), result.workflowKey(), result.currentStep(), result.status().name(),
            result.data(), result.version(),
            Timestamp.from(result.createdAt()), Timestamp.from(result.updatedAt()), Timestamp.from(Instant.now()));
    }

    @Override
    public FileSet createFileSet(FileSet fileSet) {
        jdbc.update("""
            insert into document_file_set
              (id, case_id, name, max_files, allowed_media_types, created_at)
            values (?, ?, ?, ?, ?, ?)
            """, fileSet.id().toString(), fileSet.caseId().toString(), fileSet.name(), fileSet.maxFiles(),
            fileSet.allowedMediaTypes(), Timestamp.from(fileSet.createdAt()));
        return fileSet;
    }

    @Override
    public List<FileSet> findFileSets(UUID caseId) {
        return jdbc.query("""
            select id, case_id, name, max_files, allowed_media_types, created_at
              from document_file_set where case_id = ? order by created_at
            """, this::fileSet, caseId.toString());
    }

    @Override
    public Optional<FileSet> findFileSet(UUID fileSetId) {
        return jdbc.query("""
            select id, case_id, name, max_files, allowed_media_types, created_at
              from document_file_set where id = ?
            """, this::fileSet, fileSetId.toString()).stream().findFirst();
    }

    @Override
    public Document addDocument(Document document) {
        jdbc.update("""
            insert into document_file
              (id, file_set_id, object_key, original_file_name, mime_type, size_bytes,
               checksum_sha256, status, created_at)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, document.id().toString(), document.fileSetId().toString(), document.objectKey(),
            document.originalFileName(), document.mimeType(), document.sizeBytes(), document.checksumSha256(),
            document.status(), Timestamp.from(document.createdAt()));
        return document;
    }

    @Override
    public List<Document> findDocuments(UUID fileSetId) {
        return jdbc.query("""
            select id, file_set_id, object_key, original_file_name, mime_type, size_bytes,
                   checksum_sha256, status, created_at
              from document_file where file_set_id = ? order by created_at
            """, (rs, row) -> new Document(
                UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("file_set_id")),
                rs.getString("object_key"), rs.getString("original_file_name"), rs.getString("mime_type"),
                rs.getLong("size_bytes"), rs.getString("checksum_sha256"), rs.getString("status"),
                instant(rs, "created_at")
            ), fileSetId.toString());
    }

    private FileSet fileSet(ResultSet rs, int row) throws SQLException {
        return new FileSet(UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("case_id")),
            rs.getString("name"), rs.getInt("max_files"), rs.getString("allowed_media_types"),
            instant(rs, "created_at"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }
}
