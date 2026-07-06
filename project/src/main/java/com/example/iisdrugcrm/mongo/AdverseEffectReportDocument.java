package com.example.iisdrugcrm.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Document-oriented model of an adverse effect report (SBP course integration).
 *
 * Modeling decision (embedding vs referencing): status history, content versions
 * and analyst notes are always read together with the report and never queried
 * standalone, so they are EMBEDDED into a single document. Effect labels are
 * denormalized into an array at sync time so aggregations can $unwind them
 * without any joins.
 */
@Document(collection = AdverseEffectReportDocument.COLLECTION)
public class AdverseEffectReportDocument {

    public static final String COLLECTION = "adverse_effect_reports";

    @Id
    private Long id;

    private String reportType;
    private String status;
    private String source;
    private String sourceLabel;
    private String severity;
    private String medicationName;
    private List<String> effectLabels = new ArrayList<>();
    private String effectDescription;
    private String symptoms;
    private String additionalNotes;
    private LocalDate symptomDate;
    private LocalDateTime createdAt;
    private String reporterUsername;
    private String reporterType;
    private String patientGender;
    private Integer patientAge;
    private Integer currentVersionNumber;
    private List<StatusChangeEntry> statusHistory = new ArrayList<>();
    private List<VersionEntry> versions = new ArrayList<>();
    private List<NoteEntry> notes = new ArrayList<>();
    private boolean seeded = false;
    private LocalDateTime syncedAt;

    public static class StatusChangeEntry {
        private String oldStatus;
        private String newStatus;
        private LocalDateTime changedAt;
        private String changedByUsername;
        private String comment;
        private String priority;

        public String getOldStatus() { return oldStatus; }
        public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public LocalDateTime getChangedAt() { return changedAt; }
        public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
        public String getChangedByUsername() { return changedByUsername; }
        public void setChangedByUsername(String changedByUsername) { this.changedByUsername = changedByUsername; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class VersionEntry {
        private Integer versionNumber;
        private boolean active;
        private LocalDateTime createdAt;
        private String createdByUsername;
        private String medicationName;
        private String severity;
        private String effectDescription;

        public Integer getVersionNumber() { return versionNumber; }
        public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getCreatedByUsername() { return createdByUsername; }
        public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
        public String getMedicationName() { return medicationName; }
        public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getEffectDescription() { return effectDescription; }
        public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }
    }

    public static class NoteEntry {
        private String authorUsername;
        private LocalDateTime createdAt;
        private String content;

        public String getAuthorUsername() { return authorUsername; }
        public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceLabel() { return sourceLabel; }
    public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public List<String> getEffectLabels() { return effectLabels; }
    public void setEffectLabels(List<String> effectLabels) { this.effectLabels = effectLabels; }
    public String getEffectDescription() { return effectDescription; }
    public void setEffectDescription(String effectDescription) { this.effectDescription = effectDescription; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public LocalDate getSymptomDate() { return symptomDate; }
    public void setSymptomDate(LocalDate symptomDate) { this.symptomDate = symptomDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }
    public String getReporterType() { return reporterType; }
    public void setReporterType(String reporterType) { this.reporterType = reporterType; }
    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }
    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }
    public Integer getCurrentVersionNumber() { return currentVersionNumber; }
    public void setCurrentVersionNumber(Integer currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; }
    public List<StatusChangeEntry> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusChangeEntry> statusHistory) { this.statusHistory = statusHistory; }
    public List<VersionEntry> getVersions() { return versions; }
    public void setVersions(List<VersionEntry> versions) { this.versions = versions; }
    public List<NoteEntry> getNotes() { return notes; }
    public void setNotes(List<NoteEntry> notes) { this.notes = notes; }
    public boolean isSeeded() { return seeded; }
    public void setSeeded(boolean seeded) { this.seeded = seeded; }
    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
