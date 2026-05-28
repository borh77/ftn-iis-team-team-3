CREATE TABLE variant_versions (
    id BIGSERIAL PRIMARY KEY,

    variant_id BIGINT NOT NULL,

    version_label VARCHAR(50) NOT NULL,
    description VARCHAR(1000),

    status VARCHAR(30) NOT NULL,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_variant_versions_variant
        FOREIGN KEY (variant_id)
        REFERENCES variants(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_variant_versions_variant_label
        UNIQUE (variant_id, version_label)
);