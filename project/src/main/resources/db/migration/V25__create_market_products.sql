CREATE TABLE market_products (
    id BIGSERIAL PRIMARY KEY,

    variant_id BIGINT NOT NULL,
    region_id BIGINT NOT NULL,

    local_name VARCHAR(255) NOT NULL,
    packaging_description VARCHAR(500),
    barcode VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_market_products_variant
        FOREIGN KEY (variant_id)
        REFERENCES variants(id),

    CONSTRAINT fk_market_products_region
        FOREIGN KEY (region_id)
        REFERENCES regions(id),

    CONSTRAINT uq_market_products_variant_region
        UNIQUE (variant_id, region_id),

    CONSTRAINT uq_market_products_barcode
        UNIQUE (barcode)
);