package com.example.iisdrugcrm.dto.adverse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class UpdateDoctorReportRequestDTO {

    @NotBlank(message = "Naziv leka je obavezan")
    private String medicationName;

    @NotBlank(message = "Ozbiljnost je obavezna")
    private String severity;

    private String source;

    @NotNull(message = "Datum simptoma je obavezan")
    private LocalDate symptomDate;

    @NotBlank(message = "Opis efekta je obavezan")
    private String effectDescription;

    private String additionalNotes;

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDate getSymptomDate() { return symptomDate; }
    public void setSymptomDate(LocalDate symptomDate) { this.symptomDate = symptomDate; }

    public String getEffectDescription() { return effectDescription; }
    public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
}
