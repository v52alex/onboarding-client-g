-- The first asynchronous deployment could acknowledge a send before RabbitMQ
-- declared the exchange. Replaying is safe because audit stores event_id uniquely.
update onboarding_outbox_event
   set status = 'RETRY',
       published_at = null,
       next_attempt_at = current_timestamp
 where status = 'PUBLISHED';
