create table onboarding_case (
    id uuid primary key,
    workflow_key varchar(100) not null,
    current_step varchar(100) not null,
    status varchar(30) not null,
    data text not null,
    version bigint not null default 0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_onboarding_case_status on onboarding_case (status);

