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

    public String getEffectDescription() { return effectDescription; }
    public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
}
