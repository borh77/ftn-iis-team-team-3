-- Glavna tabela za naloge neželjenih efekata (IS-A bazna tabela)
CREATE TABLE adverse_effect_reports (
    id           BIGSERIAL PRIMARY KEY,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    source       VARCHAR(50),                          -- web / mobile / api
    severity     VARCHAR(50),                          -- MILD / MODERATE / SEVERE / CRITICAL
    symptom_date DATE,
    status       VARCHAR(30)  NOT NULL DEFAULT 'SUBMITTED',
    reporter_id  BIGINT       NOT NULL REFERENCES users(id),
    medication_name VARCHAR(255) NOT NULL,
    report_type  VARCHAR(20)  NOT NULL                 -- DOCTOR / PATIENT (discriminator)
);

-- Nalozi lekara (IS-A AdverseEffectReport)
CREATE TABLE doctor_reports (
    id                 BIGINT PRIMARY KEY REFERENCES adverse_effect_reports(id),
    effect_description TEXT,
    additional_notes   TEXT
);

-- Nalozi pacijenata (IS-A AdverseEffectReport)
CREATE TABLE patient_reports (
    id              BIGINT PRIMARY KEY REFERENCES adverse_effect_reports(id),
    symptoms        TEXT,
    additional_desc TEXT
);
