create table users (
    id bigserial primary key,
    username varchar(100) not null unique,
    password_hash varchar(255) not null,
    email varchar(255) not null unique,
    role varchar(50) not null,
    is_active boolean not null default true,
    has_changed_password boolean not null default false
);