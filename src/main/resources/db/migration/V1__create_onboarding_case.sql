create table onboarding_case (
    id varchar(36) primary key,
    workflow_key varchar(100) not null,
    current_step varchar(100) not null,
    status varchar(30) not null,
    data text not null,
    version bigint not null default 0,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null
);

create index idx_onboarding_case_status on onboarding_case (status);
