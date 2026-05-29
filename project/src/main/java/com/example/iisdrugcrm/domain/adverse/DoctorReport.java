package com.example.iisdrugcrm.domain.adverse;

import jakarta.persistence.*;

@Entity
@Table(name = "doctor_reports")
@DiscriminatorValue("DOCTOR")
public class DoctorReport extends AdverseEffectReport {

    @Column(name = "effect_description", columnDefinition = "TEXT")
    private String effectDescription;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "patient_gender")
    private String patientGender;

    @Column(name = "patient_age")
    private Integer patientAge;

    public String getEffectDescription() { return effectDescription; }
    public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }
}
