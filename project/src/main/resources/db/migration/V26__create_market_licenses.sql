CREATE TABLE market_licenses (
    id BIGSERIAL PRIMARY KEY,

    market_product_id BIGINT NOT NULL,
    variant_version_id BIGINT NOT NULL,

    license_number VARCHAR(100) NOT NULL UNIQUE,
    issued_at DATE,
    valid_until DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_market_licenses_market_product
        FOREIGN KEY (market_product_id)
        REFERENCES market_products(id),

    CONSTRAINT fk_market_licenses_variant_version
        FOREIGN KEY (variant_version_id)
        REFERENCES variant_versions(id),

    CONSTRAINT uq_market_licenses_market_product_version
        UNIQUE (market_product_id, variant_version_id)
);