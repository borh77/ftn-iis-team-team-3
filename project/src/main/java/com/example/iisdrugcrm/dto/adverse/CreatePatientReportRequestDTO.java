package com.example.iisdrugcrm.dto.adverse;

import jakarta.validation.constraints.NotBlank;

public class CreatePatientReportRequestDTO {

    @NotBlank(message = "Naziv leka je obavezan")
    private String medicationName;

    @NotBlank(message = "Simptomi su obavezni")
    private String symptoms;

    private String additionalDesc;

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getAdditionalDesc() { return additionalDesc; }
    public void setAdditionalDesc(String additionalDesc) { this.additionalDesc = additionalDesc; }
}
