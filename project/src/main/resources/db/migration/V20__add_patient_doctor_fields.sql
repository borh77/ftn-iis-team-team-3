-- Add gender, age, symptomDate to patient_reports
ALTER TABLE patient_reports
    ADD COLUMN patient_gender VARCHAR(10),
    ADD COLUMN patient_age INT,
    ADD COLUMN symptom_date DATE;

-- Add patient gender and age to doctor_reports
ALTER TABLE doctor_reports
    ADD COLUMN patient_gender VARCHAR(10),
    ADD COLUMN patient_age INT;
