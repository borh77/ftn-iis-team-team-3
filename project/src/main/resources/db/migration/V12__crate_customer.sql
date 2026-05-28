create table customers (
    id bigserial primary key,
    name varchar(150) not null,
    email varchar(255) not null unique,
    phone varchar(50),
    website varchar(255),
    address varchar(255),
    status varchar(50) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);