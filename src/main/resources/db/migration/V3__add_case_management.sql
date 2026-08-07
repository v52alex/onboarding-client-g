create table onboarding_case_review (
    case_id varchar(36) primary key,
    review_status varchar(30) not null,
    assigned_to varchar(150),
    decision_reason varchar(500),
    decided_by varchar(150),
    decided_at timestamp(6),
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    constraint fk_case_review_case foreign key (case_id) references onboarding_case (id)
);

create index idx_case_review_queue on onboarding_case_review (review_status, updated_at);
create index idx_case_review_assignee on onboarding_case_review (assigned_to, review_status);

create table onboarding_case_review_event (
    id varchar(36) primary key,
    case_id varchar(36) not null,
    event_type varchar(60) not null,
    actor_id varchar(150) not null,
    detail varchar(500),
    occurred_at timestamp(6) not null,
    constraint fk_case_review_event_review foreign key (case_id) references onboarding_case_review (case_id)
);

create index idx_case_review_event_time on onboarding_case_review_event (case_id, occurred_at);

insert into onboarding_case_review
    (case_id, review_status, created_at, updated_at)
select id, 'PENDING', updated_at, updated_at
from onboarding_case
where status = 'COMPLETED';
