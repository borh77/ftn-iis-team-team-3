package com.example.iisdrugcrm.domain.adverse;

import jakarta.persistence.*;

@Entity
@Table(name = "patient_reports")
@DiscriminatorValue("PATIENT")
public class PatientReport extends AdverseEffectReport {

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "additional_desc", columnDefinition = "TEXT")
    private String additionalDesc;

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getAdditionalDesc() { return additionalDesc; }
    public void setAdditionalDesc(String additionalDesc) { this.additionalDesc = additionalDesc; }
}
