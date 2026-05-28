CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),

    subcategory_id BIGINT NOT NULL,
    therapeutic_area_id BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_products_subcategory
        FOREIGN KEY (subcategory_id)
        REFERENCES subcategories(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_products_therapeutic_area
        FOREIGN KEY (therapeutic_area_id)
        REFERENCES therapeutic_areas(id)
        ON DELETE RESTRICT
);