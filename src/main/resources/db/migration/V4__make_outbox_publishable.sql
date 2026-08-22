alter table onboarding_outbox_event
    add column last_error varchar(1000);

alter table onboarding_outbox_event
    add column next_attempt_at timestamp(6);

alter table onboarding_outbox_event
    add column locked_at timestamp(6);

update onboarding_outbox_event
   set next_attempt_at = created_at
 where next_attempt_at is null;

create index idx_outbox_publishable
    on onboarding_outbox_event (status, next_attempt_at, created_at);
