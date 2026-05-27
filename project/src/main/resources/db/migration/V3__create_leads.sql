create table leads (
    id bigserial primary key,
    name varchar(150) not null,
    email varchar(255) not null unique,
    address varchar(255),
    source varchar(100),
    score integer not null default 0,
    status varchar(50) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);