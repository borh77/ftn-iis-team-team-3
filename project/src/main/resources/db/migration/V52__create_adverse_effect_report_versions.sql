CREATE TABLE IF NOT EXISTS adverse_effect_report_versions (
    id BIGSERIAL PRIMARY KEY,
    report_id BIGINT NOT NULL REFERENCES adverse_effect_reports(id),
    version_number INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    medication_name VARCHAR(255) NOT NULL,
    source VARCHAR(50),
    severity VARCHAR(50),
    symptom_date DATE,
    effect_description TEXT,
    additional_notes TEXT,
    patient_gender VARCHAR(20),
    patient_age INT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    CONSTRAINT uq_adverse_report_version_number UNIQUE (report_id, version_number)
);

ALTER TABLE adverse_effect_reports
    ADD COLUMN IF NOT EXISTS current_version_id BIGINT;

ALTER TABLE adverse_effect_reports
    DROP CONSTRAINT IF EXISTS fk_adverse_report_current_version;

ALTER TABLE adverse_effect_reports
    ADD CONSTRAINT fk_adverse_report_current_version
        FOREIGN KEY (current_version_id)
        REFERENCES adverse_effect_report_versions(id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_adverse_report_one_active_version
    ON adverse_effect_report_versions(report_id)
    WHERE active = TRUE;

INSERT INTO adverse_effect_report_versions (
    report_id,
    version_number,
    active,
    medication_name,
    source,
    severity,
    symptom_date,
    effect_description,
    additional_notes,
    patient_gender,
    patient_age,
    created_at,
    created_by_id
)
SELECT
    aer.id,
    1,
    TRUE,
    aer.medication_name,
    aer.source,
    aer.severity,
    aer.symptom_date,
    dr.effect_description,
    dr.additional_notes,
    dr.patient_gender,
    dr.patient_age,
    aer.created_at,
    aer.reporter_id
FROM adverse_effect_reports aer
JOIN doctor_reports dr ON dr.id = aer.id
WHERE NOT EXISTS (
    SELECT 1
    FROM adverse_effect_report_versions existing_version
    WHERE existing_version.report_id = aer.id
);

UPDATE adverse_effect_reports aer
SET current_version_id = latest_version.id
FROM adverse_effect_report_versions latest_version
WHERE latest_version.report_id = aer.id
  AND latest_version.active = TRUE
  AND aer.current_version_id IS NULL;
