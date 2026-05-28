CREATE TABLE variant_version_ingredients (
    id BIGSERIAL PRIMARY KEY,

    variant_version_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,

    amount NUMERIC(12, 4) NOT NULL,
    unit VARCHAR(30) NOT NULL,

    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP,

    CONSTRAINT fk_vvi_variant_version
        FOREIGN KEY (variant_version_id)
        REFERENCES variant_versions(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_vvi_ingredient
        FOREIGN KEY (ingredient_id)
        REFERENCES ingredients(id)
        ON DELETE RESTRICT,

    CONSTRAINT uq_vvi_version_ingredient
        UNIQUE (variant_version_id, ingredient_id)
);