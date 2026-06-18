CREATE TABLE variant_version_lifecycle_history (
    id BIGSERIAL PRIMARY KEY,

    variant_version_id BIGINT NOT NULL,

    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,

    changed_at TIMESTAMP NOT NULL,
    changed_by BIGINT,

    reason VARCHAR(1000),
    automatic_transition BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_variant_version_lifecycle_history_version
        FOREIGN KEY (variant_version_id)
        REFERENCES variant_versions(id)
);