package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.domain.CaseManagementRecords.CasePage;
import com.v52alex.onboarding.domain.CaseManagementRecords.CaseSummary;
import com.v52alex.onboarding.domain.CaseManagementRecords.Review;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewEvent;
import com.v52alex.onboarding.domain.CaseManagementRecords.ReviewStatus;
import com.v52alex.onboarding.domain.CaseManagementRepository;
import com.v52alex.onboarding.domain.OnboardingStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcCaseManagementRepository implements CaseManagementRepository {

    private final JdbcTemplate jdbc;

    JdbcCaseManagementRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ensurePendingReview(UUID caseId) {
        Instant now = Instant.now();
        jdbc.update("""
            insert into onboarding_case_review
                (case_id, review_status, created_at, updated_at)
            select ?, 'PENDING', ?, ?
            where exists (select 1 from onboarding_case where id = ? and status = 'COMPLETED')
              and not exists (select 1 from onboarding_case_review where case_id = ?)
            """, caseId.toString(), Timestamp.from(now), Timestamp.from(now),
            caseId.toString(), caseId.toString());
    }

    @Override
    public CasePage search(ReviewStatus status, String assignedTo, int page, int size) {
        StringBuilder where = new StringBuilder(" where 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (status != null) {
            where.append(" and r.review_status = ?");
            parameters.add(status.name());
        }
        if (assignedTo != null && !assignedTo.isBlank()) {
            where.append(" and r.assigned_to = ?");
            parameters.add(assignedTo);
        }

        long total = jdbc.queryForObject(
            "select count(*) from onboarding_case c join onboarding_case_review r on r.case_id = c.id"
                + where,
            Long.class,
            parameters.toArray()
        );
        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(size);
        pageParameters.add(page * size);
        List<CaseSummary> content = jdbc.query("""
                select c.id, c.workflow_key, c.current_step, c.status, r.review_status,
                       r.assigned_to, c.created_at, r.updated_at
                  from onboarding_case c
                  join onboarding_case_review r on r.case_id = c.id
                """ + where + " order by r.updated_at desc limit ? offset ?",
            this::summary,
            pageParameters.toArray());
        int totalPages = total == 0 ? 0 : (int) ((total + size - 1) / size);
        return new CasePage(content, page, size, total, totalPages);
    }

    @Override
    public Optional<Review> findReview(UUID caseId) {
        return jdbc.query("""
            select case_id, review_status, assigned_to, decision_reason, decided_by,
                   decided_at, created_at, updated_at
              from onboarding_case_review where case_id = ?
            """, this::review, caseId.toString()).stream().findFirst();
    }

    @Override
    public Review assign(UUID caseId, String assignedTo, String actorId) {
        Instant now = Instant.now();
        int updated = jdbc.update("""
            update onboarding_case_review
               set assigned_to = ?, updated_at = ?
             where case_id = ? and review_status = 'PENDING' and assigned_to is null
            """, assignedTo, Timestamp.from(now), caseId.toString());
        if (updated != 1) {
            throw new IllegalStateException("Case cannot be assigned in its current review state");
        }
        recordEvent(caseId, "CASE_ASSIGNED", actorId, assignedTo, now);
        return findReview(caseId).orElseThrow();
    }

    @Override
    public Review decide(UUID caseId, ReviewStatus decision, String reason, String actorId) {
        Instant now = Instant.now();
        int updated = jdbc.update("""
            update onboarding_case_review
               set review_status = ?, decision_reason = ?, decided_by = ?, decided_at = ?, updated_at = ?
             where case_id = ? and review_status = 'PENDING' and assigned_to = ?
            """, decision.name(), reason, actorId, Timestamp.from(now), Timestamp.from(now),
            caseId.toString(), actorId);
        if (updated != 1) {
            throw new IllegalStateException("Case must be assigned to the current operator before deciding");
        }
        recordEvent(caseId, "CASE_" + decision.name(), actorId, reason, now);
        return findReview(caseId).orElseThrow();
    }

    @Override
    public List<ReviewEvent> findEvents(UUID caseId) {
        return jdbc.query("""
            select id, case_id, event_type, actor_id, detail, occurred_at
              from onboarding_case_review_event where case_id = ? order by occurred_at
            """, (rs, row) -> new ReviewEvent(
                UUID.fromString(rs.getString("id")), UUID.fromString(rs.getString("case_id")),
                rs.getString("event_type"), rs.getString("actor_id"), rs.getString("detail"),
                instant(rs, "occurred_at")
            ), caseId.toString());
    }

    private void recordEvent(UUID caseId, String eventType, String actorId, String detail, Instant occurredAt) {
        jdbc.update("""
            insert into onboarding_case_review_event
                (id, case_id, event_type, actor_id, detail, occurred_at)
            values (?, ?, ?, ?, ?, ?)
            """, UUID.randomUUID().toString(), caseId.toString(), eventType, actorId, detail,
            Timestamp.from(occurredAt));
    }

    private CaseSummary summary(ResultSet rs, int row) throws SQLException {
        return new CaseSummary(
            UUID.fromString(rs.getString("id")), rs.getString("workflow_key"),
            rs.getString("current_step"), OnboardingStatus.valueOf(rs.getString("status")),
            ReviewStatus.valueOf(rs.getString("review_status")), rs.getString("assigned_to"),
            instant(rs, "created_at"), instant(rs, "updated_at")
        );
    }

    private Review review(ResultSet rs, int row) throws SQLException {
        return new Review(
            UUID.fromString(rs.getString("case_id")), ReviewStatus.valueOf(rs.getString("review_status")),
            rs.getString("assigned_to"), rs.getString("decision_reason"), rs.getString("decided_by"),
            nullableInstant(rs, "decided_at"), instant(rs, "created_at"), instant(rs, "updated_at")
        );
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }

    private static Instant nullableInstant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
