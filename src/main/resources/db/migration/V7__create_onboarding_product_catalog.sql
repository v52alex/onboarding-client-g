create table onboarding_product (
    id varchar(100) primary key,
    code varchar(100) not null,
    name varchar(150) not null,
    description varchar(500) not null,
    active boolean not null default true,
    display_order int not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6) not null,
    constraint uk_onboarding_product_code unique (code)
);

create index idx_onboarding_product_active_order
    on onboarding_product (active, display_order);

insert into onboarding_product
    (id, code, name, description, active, display_order, created_at, updated_at)
values
    ('checking-account', 'CHECKING_ACCOUNT', 'Checking account',
     'An everyday account for payments and transfers.', true, 10, current_timestamp, current_timestamp),
    ('savings-account', 'SAVINGS_ACCOUNT', 'Savings account',
     'An account designed to save and earn interest.', true, 20, current_timestamp, current_timestamp);
