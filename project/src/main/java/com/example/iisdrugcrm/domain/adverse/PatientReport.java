package com.example.iisdrugcrm.domain.adverse;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patient_reports")
@DiscriminatorValue("PATIENT")
public class PatientReport extends AdverseEffectReport {

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "additional_desc", columnDefinition = "TEXT")
    private String additionalDesc;

    @Column(name = "patient_gender")
    private String patientGender;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(name = "symptom_date")
    private LocalDate symptomDate;

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getAdditionalDesc() { return additionalDesc; }
    public void setAdditionalDesc(String additionalDesc) { this.additionalDesc = additionalDesc; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public LocalDate getSymptomDate() { return symptomDate; }
    public void setSymptomDate(LocalDate symptomDate) { this.symptomDate = symptomDate; }
}
