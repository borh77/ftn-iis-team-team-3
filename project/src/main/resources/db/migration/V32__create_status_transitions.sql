CREATE TABLE IF NOT EXISTS status_transitions (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES adverse_effect_reports(id),
    changed_by_id BIGINT NOT NULL REFERENCES users(id),
    old_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    comment TEXT,
    priority VARCHAR(20),
    closure_reason TEXT,
    verdict TEXT
);

