-- Events written before V6 did not initialize next_attempt_at. The publisher
-- intentionally selects only scheduled events, so make outstanding work eligible
-- without replaying events that have already been published.
update onboarding_outbox_event
   set next_attempt_at = created_at
 where status in ('PENDING', 'RETRY')
   and next_attempt_at is null;
