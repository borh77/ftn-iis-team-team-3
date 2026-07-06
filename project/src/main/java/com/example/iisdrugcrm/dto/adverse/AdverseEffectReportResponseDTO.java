package com.example.iisdrugcrm.dto.adverse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdverseEffectReportResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private String source;
    private String severity;
    private LocalDate symptomDate;
    private String status;
    private String medicationName;
    private String reportType;       // DOCTOR or PATIENT
    private String reporterUsername;
    private Long currentVersionId;
    private Integer currentVersionNumber;

    // Fields specific to DoctorReport
    private String effectDescription;
    private String additionalNotes;

    // Fields specific to PatientReport
    private String symptoms;
    private String additionalDesc;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDate getSymptomDate() { return symptomDate; }
    public void setSymptomDate(LocalDate symptomDate) { this.symptomDate = symptomDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public Long getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(Long currentVersionId) { this.currentVersionId = currentVersionId; }

    public Integer getCurrentVersionNumber() { return currentVersionNumber; }
    public void setCurrentVersionNumber(Integer currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; }

    public String getEffectDescription() { return effectDescription; }
    public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getAdditionalDesc() { return additionalDesc; }
    public void setAdditionalDesc(String additionalDesc) { this.additionalDesc = additionalDesc; }

    // Shared patient info
    private String patientGender;
    private Integer patientAge;

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

}
