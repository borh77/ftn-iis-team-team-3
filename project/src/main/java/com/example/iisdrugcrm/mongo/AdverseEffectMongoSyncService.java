package com.example.iisdrugcrm.mongo;

import com.example.iisdrugcrm.domain.adverse.AdverseEffectReport;
import com.example.iisdrugcrm.domain.adverse.AdverseEffectReportVersion;
import com.example.iisdrugcrm.domain.adverse.AnalystNote;
import com.example.iisdrugcrm.domain.adverse.DoctorReport;
import com.example.iisdrugcrm.domain.adverse.PatientReport;
import com.example.iisdrugcrm.domain.adverse.StatusTransition;
import com.example.iisdrugcrm.repository.adverse.AdverseEffectReportRepository;
import com.example.iisdrugcrm.repository.adverse.AdverseEffectReportVersionRepository;
import com.example.iisdrugcrm.repository.adverse.AnalystNoteRepository;
import com.example.iisdrugcrm.repository.adverse.StatusTransitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mirrors adverse effect reports from PostgreSQL (transactional store) into
 * MongoDB (analytical store) as embedded documents. Mongo failures are logged
 * and swallowed so the primary workflow is never disrupted.
 */
@Service
public class AdverseEffectMongoSyncService {

    private static final Logger log = LoggerFactory.getLogger(AdverseEffectMongoSyncService.class);

    private final MongoTemplate mongoTemplate;
    private final AdverseEffectReportRepository reportRepository;
    private final AdverseEffectReportVersionRepository versionRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final AnalystNoteRepository analystNoteRepository;

    public AdverseEffectMongoSyncService(
            MongoTemplate mongoTemplate,
            AdverseEffectReportRepository reportRepository,
            AdverseEffectReportVersionRepository versionRepository,
            StatusTransitionRepository statusTransitionRepository,
            AnalystNoteRepository analystNoteRepository) {
        this.mongoTemplate = mongoTemplate;
        this.reportRepository = reportRepository;
        this.versionRepository = versionRepository;
        this.statusTransitionRepository = statusTransitionRepository;
        this.analystNoteRepository = analystNoteRepository;
    }

    /** Upserts a single report into MongoDB. Never throws. */
    public void syncReportSafely(Long reportId) {
        try {
            reportRepository.findById(reportId)
                    .ifPresent(report -> mongoTemplate.save(toDocument(report)));
        } catch (Exception exception) {
            log.warn("MongoDB sync skipped for report {}: {}", reportId, exception.getMessage());
        }
    }

    /** Backfills every report from PostgreSQL into MongoDB. Returns synced count. */
    public long syncAll() {
        List<AdverseEffectReport> reports = reportRepository.findAll();
        long synced = 0;
        for (AdverseEffectReport report : reports) {
            mongoTemplate.save(toDocument(report));
            synced++;
        }
        log.info("MongoDB backfill finished: {} reports synced.", synced);
        return synced;
    }

    private AdverseEffectReportDocument toDocument(AdverseEffectReport report) {
        AdverseEffectReportDocument document = new AdverseEffectReportDocument();
        document.setId(report.getId());
        document.setStatus(report.getStatus().name());
        document.setSource(report.getSource());
        document.setSourceLabel(sourceLabel(report));
        document.setSeverity(report.getSeverity());
        document.setMedicationName(report.getMedicationName());
        document.setSymptomDate(report.getSymptomDate());
        document.setCreatedAt(report.getCreatedAt());
        document.setReporterUsername(report.getReporter().getUsername());
        document.setSyncedAt(LocalDateTime.now());

        if (report instanceof DoctorReport doctorReport) {
            document.setReportType("DOCTOR");
            document.setReporterType("Doctor");
            document.setEffectDescription(doctorReport.getEffectDescription());
            document.setAdditionalNotes(doctorReport.getAdditionalNotes());
            document.setPatientGender(doctorReport.getPatientGender());
            document.setPatientAge(doctorReport.getPatientAge());
            document.setEffectLabels(doctorEffectLabels(doctorReport));
            document.setVersions(versionEntries(report.getId()));
            if (report.getCurrentVersion() != null) {
                document.setCurrentVersionNumber(report.getCurrentVersion().getVersionNumber());
            }
        } else if (report instanceof PatientReport patientReport) {
            document.setReportType("PATIENT");
            document.setReporterType("Patient");
            document.setSymptoms(patientReport.getSymptoms());
            document.setAdditionalNotes(patientReport.getAdditionalDesc());
            document.setPatientGender(patientReport.getPatientGender());
            document.setPatientAge(patientReport.getPatientAge());
            document.setSymptomDate(patientReport.getSymptomDate());
            document.setEffectLabels(splitSymptoms(patientReport.getSymptoms()));
        }

        document.setStatusHistory(statusEntries(report.getId()));
        document.setNotes(noteEntries(report.getId()));
        return document;
    }

    private List<AdverseEffectReportDocument.StatusChangeEntry> statusEntries(Long reportId) {
        List<AdverseEffectReportDocument.StatusChangeEntry> entries = new ArrayList<>();
        for (StatusTransition transition : statusTransitionRepository.findByReportIdOrderByChangedAtAsc(reportId)) {
            AdverseEffectReportDocument.StatusChangeEntry entry = new AdverseEffectReportDocument.StatusChangeEntry();
            entry.setOldStatus(transition.getOldStatus());
            entry.setNewStatus(transition.getNewStatus());
            entry.setChangedAt(transition.getChangedAt());
            entry.setChangedByUsername(transition.getChangedBy().getUsername());
            entry.setComment(transition.getComment());
            entry.setPriority(transition.getPriority());
            entries.add(entry);
        }
        return entries;
    }

    private List<AdverseEffectReportDocument.VersionEntry> versionEntries(Long reportId) {
        List<AdverseEffectReportDocument.VersionEntry> entries = new ArrayList<>();
        for (AdverseEffectReportVersion version : versionRepository.findByReportIdOrderByVersionNumberDesc(reportId)) {
            AdverseEffectReportDocument.VersionEntry entry = new AdverseEffectReportDocument.VersionEntry();
            entry.setVersionNumber(version.getVersionNumber());
            entry.setActive(version.isActive());
            entry.setCreatedAt(version.getCreatedAt());
            entry.setCreatedByUsername(version.getCreatedBy().getUsername());
            entry.setMedicationName(version.getMedicationName());
            entry.setSeverity(version.getSeverity());
            entry.setEffectDescription(version.getEffectDescription());
            entries.add(entry);
        }
        return entries;
    }

    private List<AdverseEffectReportDocument.NoteEntry> noteEntries(Long reportId) {
        List<AdverseEffectReportDocument.NoteEntry> entries = new ArrayList<>();
        for (AnalystNote note : analystNoteRepository.findByReportIdOrderByCreatedAtAsc(reportId)) {
            AdverseEffectReportDocument.NoteEntry entry = new AdverseEffectReportDocument.NoteEntry();
            entry.setAuthorUsername(note.getAuthor().getUsername());
            entry.setCreatedAt(note.getCreatedAt());
            entry.setContent(note.getContent());
            entries.add(entry);
        }
        return entries;
    }

    private List<String> doctorEffectLabels(DoctorReport doctorReport) {
        String description = trimToNull(doctorReport.getEffectDescription());
        if (description == null) {
            return List.of("Doctor free-text effect");
        }
        return List.of(shorten(description, 70));
    }

    private String sourceLabel(AdverseEffectReport report) {
        String source = trimToNull(report.getSource());
        if (source != null) {
            return toTitleCase(source);
        }
        if (report instanceof PatientReport) {
            return "Patient Portal";
        }
        return "Unknown Source";
    }

    private List<String> splitSymptoms(String symptoms) {
        String trimmed = trimToNull(symptoms);
        if (trimmed == null) {
            return List.of("Unspecified symptom");
        }

        String[] parts = trimmed.split("[,;\\n]+");
        List<String> labels = new ArrayList<>();
        for (String part : parts) {
            String label = trimToNull(part);
            if (label != null) {
                labels.add(toTitleCase(label));
            }
        }
        return labels.isEmpty() ? List.of("Unspecified symptom") : labels;
    }

    private String shorten(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String toTitleCase(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
