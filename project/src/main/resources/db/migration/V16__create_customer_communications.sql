create table customer_communications (
    id bigserial primary key,
    customer_id bigint not null,
    type varchar(50) not null,
    communication_date timestamp not null,
    summary varchar(1000) not null,

    constraint fk_customer_communication_customer
        foreign key (customer_id)
        references customers(id)
);