ALTER TABLE patient_reports
    ADD COLUMN IF NOT EXISTS patient_gender VARCHAR(10),
    ADD COLUMN IF NOT EXISTS patient_age INT,
    ADD COLUMN IF NOT EXISTS symptom_date DATE;

ALTER TABLE doctor_reports
    ADD COLUMN IF NOT EXISTS patient_gender VARCHAR(10),
    ADD COLUMN IF NOT EXISTS patient_age INT;

