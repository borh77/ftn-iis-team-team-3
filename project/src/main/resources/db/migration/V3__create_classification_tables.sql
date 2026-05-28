CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP
);

CREATE TABLE subcategories (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_subcategories_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_subcategories_category_name
        UNIQUE (category_id, name)
);

CREATE TABLE therapeutic_areas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP
);