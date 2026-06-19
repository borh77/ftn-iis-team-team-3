CREATE TABLE IF NOT EXISTS analyst_notes (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES adverse_effect_reports(id),
    author_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

