CREATE TABLE market_license_history (
    id BIGSERIAL PRIMARY KEY,

    market_license_id BIGINT NOT NULL,

    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,

    changed_at TIMESTAMP NOT NULL,
    changed_by BIGINT,

    note VARCHAR(1000),

    CONSTRAINT fk_market_license_history_license
        FOREIGN KEY (market_license_id)
        REFERENCES market_licenses(id)
);