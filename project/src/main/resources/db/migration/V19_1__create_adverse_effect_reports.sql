CREATE TABLE IF NOT EXISTS adverse_effect_reports (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    source VARCHAR(50),
    severity VARCHAR(50),
    symptom_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    medication_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS doctor_reports (
    id BIGINT PRIMARY KEY REFERENCES adverse_effect_reports(id),
    effect_description TEXT,
    additional_notes TEXT
);

CREATE TABLE IF NOT EXISTS patient_reports (
    id BIGINT PRIMARY KEY REFERENCES adverse_effect_reports(id),
    symptoms TEXT,
    additional_desc TEXT
);

