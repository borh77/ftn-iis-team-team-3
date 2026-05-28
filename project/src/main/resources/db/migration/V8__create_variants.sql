CREATE TABLE variants (
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,

    form VARCHAR(100) NOT NULL,
    dosage VARCHAR(100) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_variants_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_variants_product_form_dosage
        UNIQUE (product_id, form, dosage)
);