create table onboarding_case_event (
    id varchar(36) primary key,
    case_id varchar(36) not null,
    event_sequence bigint not null,
    event_type varchar(60) not null,
    action_name varchar(100),
    previous_step varchar(100),
    resulting_step varchar(100) not null,
    outcome varchar(60),
    actor_id varchar(150),
    correlation_id varchar(100),
    metadata text not null,
    occurred_at timestamp(6) not null,
    constraint fk_case_event_case foreign key (case_id) references onboarding_case (id),
    constraint uk_case_event_sequence unique (case_id, event_sequence)
);

create index idx_case_event_case_time on onboarding_case_event (case_id, occurred_at);

create table onboarding_consent (
    id varchar(36) primary key,
    case_id varchar(36) not null,
    consent_type varchar(60) not null,
    document_version varchar(60) not null,
    accepted boolean not null,
    actor_id varchar(150),
    correlation_id varchar(100),
    evidence text not null,
    accepted_at timestamp(6) not null,
    constraint fk_consent_case foreign key (case_id) references onboarding_case (id),
    constraint uk_case_consent_version unique (case_id, consent_type, document_version)
);

create table onboarding_idempotency_record (
    id varchar(36) primary key,
    case_id varchar(36) not null,
    action_name varchar(100) not null,
    idempotency_key varchar(150) not null,
    request_hash varchar(64) not null,
    result_workflow_key varchar(100) not null,
    result_step varchar(100) not null,
    result_status varchar(30) not null,
    result_data text not null,
    result_version bigint not null,
    result_created_at timestamp(6) not null,
    result_updated_at timestamp(6) not null,
    created_at timestamp(6) not null,
    constraint fk_idempotency_case foreign key (case_id) references onboarding_case (id),
    constraint uk_case_action_idempotency unique (case_id, action_name, idempotency_key)
);

create table onboarding_outbox_event (
    id varchar(36) primary key,
    aggregate_id varchar(36) not null,
    event_type varchar(100) not null,
    payload text not null,
    status varchar(20) not null,
    attempts integer not null default 0,
    created_at timestamp(6) not null,
    published_at timestamp(6),
    constraint fk_outbox_case foreign key (aggregate_id) references onboarding_case (id)
);

create index idx_outbox_status_created on onboarding_outbox_event (status, created_at);

create table document_file_set (
    id varchar(36) primary key,
    case_id varchar(36) not null,
    name varchar(120) not null,
    max_files integer not null,
    allowed_media_types varchar(500) not null,
    created_at timestamp(6) not null,
    constraint fk_file_set_case foreign key (case_id) references onboarding_case (id),
    constraint uk_case_file_set_name unique (case_id, name)
);

create table document_file (
    id varchar(36) primary key,
    file_set_id varchar(36) not null,
    object_key varchar(500) not null,
    original_file_name varchar(255) not null,
    mime_type varchar(150) not null,
    size_bytes bigint not null,
    checksum_sha256 varchar(64) not null,
    status varchar(30) not null,
    created_at timestamp(6) not null,
    constraint fk_document_file_set foreign key (file_set_id) references document_file_set (id),
    constraint uk_document_object_key unique (object_key)
);

create index idx_document_file_set on document_file (file_set_id);
