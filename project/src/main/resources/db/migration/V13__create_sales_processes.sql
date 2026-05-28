create table sales_processes (
    id bigserial primary key,
    customer_id bigint not null,
    title varchar(200) not null,
    current_stage varchar(80) not null,
    status varchar(50) not null,
    outcome varchar(50),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,

    constraint fk_sales_process_customer
        foreign key (customer_id)
        references customers(id)
);