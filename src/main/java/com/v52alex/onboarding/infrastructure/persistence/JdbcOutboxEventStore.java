package com.v52alex.onboarding.infrastructure.persistence;

import com.v52alex.onboarding.application.OutboxEvent;
import com.v52alex.onboarding.application.OutboxEventStore;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class JdbcOutboxEventStore implements OutboxEventStore {
    private final JdbcTemplate jdbc;

    JdbcOutboxEventStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<OutboxEvent> findPublishable(Instant now, int batchSize) {
        return jdbc.query("""
            select id, aggregate_id, event_type, payload, attempts, created_at
              from onboarding_outbox_event
             where status in ('PENDING', 'RETRY') and next_attempt_at <= ?
             order by created_at asc limit ?
            """, (rs, row) -> new OutboxEvent(UUID.fromString(rs.getString("id")),
                rs.getString("aggregate_id"), rs.getString("event_type"), rs.getString("payload"),
                rs.getInt("attempts"), rs.getTimestamp("created_at").toInstant()),
            Timestamp.from(now), batchSize);
    }

    @Override
    public boolean claim(UUID eventId, Instant now, Instant staleBefore) {
        return jdbc.update("""
            update onboarding_outbox_event
               set status = 'PROCESSING', locked_at = ?
             where id = ?
               and (status in ('PENDING', 'RETRY') or (status = 'PROCESSING' and locked_at < ?))
            """, Timestamp.from(now), eventId.toString(), Timestamp.from(staleBefore)) == 1;
    }

    @Override
    public void markPublished(UUID eventId, Instant publishedAt) {
        jdbc.update("""
            update onboarding_outbox_event
               set status = 'PUBLISHED', published_at = ?, locked_at = null, last_error = null
             where id = ?
            """, Timestamp.from(publishedAt), eventId.toString());
    }

    @Override
    public void markFailed(UUID eventId, Instant nextAttemptAt, String error) {
        jdbc.update("""
            update onboarding_outbox_event
               set status = 'RETRY', attempts = attempts + 1, next_attempt_at = ?, locked_at = null,
                   last_error = ?
             where id = ?
            """, Timestamp.from(nextAttemptAt), error.substring(0, Math.min(error.length(), 1000)),
            eventId.toString());
    }
}
