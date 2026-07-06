package com.example.iisdrugcrm.service.adverse;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.adverse.AdverseEffectReport;
import com.example.iisdrugcrm.domain.adverse.AdverseEffectReportVersion;
import com.example.iisdrugcrm.domain.adverse.AnalystNote;
import com.example.iisdrugcrm.domain.adverse.DoctorReport;
import com.example.iisdrugcrm.domain.adverse.PatientReport;
import com.example.iisdrugcrm.domain.adverse.ReportStatus;
import com.example.iisdrugcrm.domain.adverse.StatusTransition;
import com.example.iisdrugcrm.dto.adverse.AddNoteRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectAnalyticsSummaryDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportVersionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AnalystNoteResponseDTO;
import com.example.iisdrugcrm.dto.adverse.ChangeStatusRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.StatusTransitionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.UpdateDoctorReportRequestDTO;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.repository.adverse.AdverseEffectReportRepository;
import com.example.iisdrugcrm.repository.adverse.AdverseEffectReportVersionRepository;
import com.example.iisdrugcrm.repository.adverse.AnalystNoteRepository;
import com.example.iisdrugcrm.repository.adverse.DoctorReportRepository;
import com.example.iisdrugcrm.repository.adverse.PatientReportRepository;
import com.example.iisdrugcrm.repository.adverse.StatusTransitionRepository;
import com.example.iisdrugcrm.service.EmailService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdverseEffectReportServiceImpl implements AdverseEffectReportService {

    private final DoctorReportRepository doctorReportRepository;
    private final PatientReportRepository patientReportRepository;
    private final AdverseEffectReportRepository reportRepository;
    private final AdverseEffectReportVersionRepository versionRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final AnalystNoteRepository analystNoteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AdverseEffectReportServiceImpl(
            DoctorReportRepository doctorReportRepository,
            PatientReportRepository patientReportRepository,
            AdverseEffectReportRepository reportRepository,
            AdverseEffectReportVersionRepository versionRepository,
            StatusTransitionRepository statusTransitionRepository,
            AnalystNoteRepository analystNoteRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.doctorReportRepository = doctorReportRepository;
        this.patientReportRepository = patientReportRepository;
        this.reportRepository = reportRepository;
        this.versionRepository = versionRepository;
        this.statusTransitionRepository = statusTransitionRepository;
        this.analystNoteRepository = analystNoteRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public AdverseEffectReportResponseDTO createDoctorReport(CreateDoctorReportRequestDTO dto, String username) {
        User reporter = findUserByUsername(username);

        DoctorReport report = new DoctorReport();
        report.setMedicationName(dto.getMedicationName());
        report.setSeverity(dto.getSeverity());
        report.setSource(dto.getSource());
        report.setSymptomDate(dto.getSymptomDate());
        report.setEffectDescription(dto.getEffectDescription());
        report.setAdditionalNotes(dto.getAdditionalNotes());
        report.setPatientGender(dto.getPatientGender());
        report.setPatientAge(dto.getPatientAge());
        report.setReporter(reporter);
        report.setStatus(ReportStatus.SUBMITTED);

        DoctorReport saved = doctorReportRepository.save(report);
        AdverseEffectReportVersion version = createActiveVersion(saved, reporter);
        saved.setCurrentVersion(version);

        return toDTO(doctorReportRepository.save(saved));
    }

    @Override
    public AdverseEffectReportResponseDTO createPatientReport(CreatePatientReportRequestDTO dto, String username) {
        User reporter = findUserByUsername(username);

        PatientReport report = new PatientReport();
        report.setMedicationName(dto.getMedicationName());
        report.setSymptoms(dto.getSymptoms());
        report.setAdditionalDesc(dto.getAdditionalDesc());
        report.setPatientGender(dto.getPatientGender());
        report.setPatientAge(dto.getPatientAge());
        report.setSymptomDate(dto.getSymptomDate());
        report.setReporter(reporter);
        report.setStatus(ReportStatus.EVIDENCED);

        return toDTO(patientReportRepository.save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdverseEffectReportResponseDTO> getMyReports(String username) {
        User reporter = findUserByUsername(username);
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporter.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdverseEffectReportResponseDTO updateDoctorReport(Long id, UpdateDoctorReportRequestDTO dto, String username) {
        DoctorReport report = doctorReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));

        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new IllegalStateException("Report can only be edited while it is SUBMITTED.");
        }

        if (!report.getReporter().getUsername().equals(username)) {
            throw new IllegalStateException("You can only edit your own reports.");
        }

        report.setMedicationName(dto.getMedicationName());
        report.setSeverity(dto.getSeverity());
        report.setSource(dto.getSource());
        report.setSymptomDate(dto.getSymptomDate());
        report.setEffectDescription(dto.getEffectDescription());
        report.setAdditionalNotes(dto.getAdditionalNotes());
        report.setPatientGender(dto.getPatientGender());
        report.setPatientAge(dto.getPatientAge());

        DoctorReport saved = doctorReportRepository.save(report);
        deactivateActiveVersions(saved.getId());
        AdverseEffectReportVersion version = createActiveVersion(saved, saved.getReporter());
        saved.setCurrentVersion(version);

        return toDTO(doctorReportRepository.save(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdverseEffectReportResponseDTO> getAllReports() {
        return getAllReportsFiltered(null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdverseEffectReportResponseDTO> getAllReportsFiltered(String status, String medicationName, String severity) {
        ReportStatus parsedStatus = parseStatusOrNull(status);
        String medicationFilter = normalizeFilter(medicationName);
        String severityFilter = normalizeFilter(severity);

        return reportRepository.findFiltered(parsedStatus, medicationFilter == null ? "" : medicationFilter, severityFilter)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdverseEffectReportResponseDTO getReportById(Long id) {
        return toDTO(findReportById(id));
    }

    @Override
    public AdverseEffectReportResponseDTO changeStatus(Long reportId, ChangeStatusRequestDTO dto, String currentUsername) {
        AdverseEffectReport report = findReportById(reportId);
        User changedBy = findUserByUsername(currentUsername);
        ReportStatus oldStatus = report.getStatus();
        ReportStatus newStatus = parseRequiredStatus(dto.getNewStatus());

        validateTransition(oldStatus, newStatus, dto);

        report.setStatus(newStatus);
        AdverseEffectReport saved = reportRepository.save(report);

        StatusTransition transition = new StatusTransition();
        transition.setReport(saved);
        transition.setChangedBy(changedBy);
        transition.setOldStatus(oldStatus.name());
        transition.setNewStatus(newStatus.name());
        transition.setChangedAt(LocalDateTime.now());
        transition.setComment(trimToNull(dto.getComment()));
        transition.setPriority(newStatus == ReportStatus.UNDER_REVIEW ? trimToNull(dto.getPriority()) : null);
        transition.setClosureReason(newStatus == ReportStatus.CLOSED ? trimToNull(dto.getClosureReason()) : null);
        transition.setVerdict(newStatus == ReportStatus.CLOSED ? trimToNull(dto.getVerdict()) : null);
        statusTransitionRepository.save(transition);

        User reporter = saved.getReporter();
        if (reporter.getRole() == UserRole.ROLE_LEKAR) {
            emailService.sendStatusChangeEmail(
                    reporter.getEmail(),
                    reporter.getUsername(),
                    saved.getId(),
                    oldStatus.name(),
                    newStatus.name(),
                    transition.getComment());
        }

        return toDTO(saved);
    }

    @Override
    public AnalystNoteResponseDTO addNote(Long reportId, AddNoteRequestDTO dto, String currentUsername) {
        AdverseEffectReport report = findReportById(reportId);
        User author = findUserByUsername(currentUsername);

        AnalystNote note = new AnalystNote();
        note.setReport(report);
        note.setAuthor(author);
        note.setContent(dto.getContent().trim());
        note.setCreatedAt(LocalDateTime.now());

        return toDTO(analystNoteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusTransitionResponseDTO> getStatusHistory(Long reportId) {
        findReportById(reportId);
        return statusTransitionRepository.findByReportIdOrderByChangedAtAsc(reportId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalystNoteResponseDTO> getNotes(Long reportId) {
        findReportById(reportId);
        return analystNoteRepository.findByReportIdOrderByCreatedAtAsc(reportId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdverseEffectReportVersionResponseDTO> getReportVersions(Long reportId, String currentUsername) {
        AdverseEffectReport report = findReportById(reportId);
        User currentUser = findUserByUsername(currentUsername);

        if (!(report instanceof DoctorReport)) {
            return List.of();
        }

        if (currentUser.getRole() == UserRole.ROLE_LEKAR
                && !report.getReporter().getUsername().equals(currentUsername)) {
            throw new IllegalStateException("You can only view versions for your own reports.");
        }

        if (currentUser.getRole() != UserRole.ROLE_LEKAR
                && currentUser.getRole() != UserRole.ROLE_FARMAKOVIGILANT) {
            throw new IllegalStateException("You are not allowed to view report versions.");
        }

        return versionRepository.findByReportIdOrderByVersionNumberDesc(reportId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdverseEffectAnalyticsSummaryDTO getAnalyticsSummary(LocalDate from, LocalDate to) {
        validateAnalyticsPeriod(from, to);

        List<AdverseEffectReport> reports = reportRepository.findAll()
                .stream()
                .filter(report -> isWithinCreatedPeriod(report, from, to))
                .collect(Collectors.toList());

        long total = reports.size();
        AdverseEffectAnalyticsSummaryDTO summary = new AdverseEffectAnalyticsSummaryDTO();
        summary.setTotalReports(total);
        summary.setDoctorReports(reports.stream().filter(DoctorReport.class::isInstance).count());
        summary.setPatientReports(reports.stream().filter(PatientReport.class::isInstance).count());
        summary.setSubmittedReports(countByStatus(reports, ReportStatus.SUBMITTED));
        summary.setUnderReviewReports(countByStatus(reports, ReportStatus.UNDER_REVIEW));
        summary.setClosedReports(countByStatus(reports, ReportStatus.CLOSED));
        summary.setEvidencedReports(countByStatus(reports, ReportStatus.EVIDENCED));
        summary.setReportsByMedication(toCountItems(countByMedication(reports), total));
        summary.setReportsByEffect(toCountItems(countByEffect(reports), total));
        summary.setReportsByStatus(toCountItems(countByStatusLabel(reports), total));
        summary.setReportsByReporterType(toCountItems(countByReporterType(reports), total));
        summary.setReportsOverTime(toTimeBuckets(reports));
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateAnalyticsPdfReport(LocalDate from, LocalDate to, String analystInterpretation) {
        AdverseEffectAnalyticsSummaryDTO summary = getAnalyticsSummary(from, to);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Paragraph title = new Paragraph("Adverse Effect Analytics Report", font(16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), font(9, Font.NORMAL)));
            document.add(new Paragraph("Period: " + periodLabel(from, to), font(10, Font.NORMAL)));
            document.add(new Paragraph(" "));

            addSummarySection(document, summary);
            addCountSection(document, "Reports by medication", summary.getReportsByMedication());
            addCountSection(document, "Reports by adverse effect", summary.getReportsByEffect());
            addCountSection(document, "Reports by status", summary.getReportsByStatus());
            addCountSection(document, "Doctor vs patient reports", summary.getReportsByReporterType());
            addSignalSection(document, summary, analystInterpretation);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate adverse effect analytics PDF report.", exception);
        }
    }

    private void addSummarySection(Document document, AdverseEffectAnalyticsSummaryDTO summary) throws Exception {
        document.add(new Paragraph("Summary", font(12, Font.BOLD)));
        PdfPTable table = table(2);
        addRow(table, "Total reports", String.valueOf(summary.getTotalReports()));
        addRow(table, "Doctor reports", String.valueOf(summary.getDoctorReports()));
        addRow(table, "Patient reports", String.valueOf(summary.getPatientReports()));
        addRow(table, "Submitted", String.valueOf(summary.getSubmittedReports()));
        addRow(table, "Under review", String.valueOf(summary.getUnderReviewReports()));
        addRow(table, "Closed", String.valueOf(summary.getClosedReports()));
        addRow(table, "Evidenced", String.valueOf(summary.getEvidencedReports()));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addCountSection(Document document, String title, List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> items) throws Exception {
        document.add(new Paragraph(title, font(12, Font.BOLD)));
        PdfPTable table = table(3);
        table.addCell(headerCell("Item"));
        table.addCell(headerCell("Count"));
        table.addCell(headerCell("Share"));

        if (items.isEmpty()) {
            PdfPCell empty = valueCell("No data for selected period.");
            empty.setColspan(3);
            table.addCell(empty);
        } else {
            for (AdverseEffectAnalyticsSummaryDTO.CountItemDTO item : items) {
                table.addCell(valueCell(item.getLabel()));
                table.addCell(valueCell(String.valueOf(item.getCount())));
                table.addCell(valueCell(item.getPercentage() + "%"));
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addSignalSection(Document document, AdverseEffectAnalyticsSummaryDTO summary, String analystInterpretation) throws Exception {
        document.add(new Paragraph("Analyst interpretation", font(12, Font.BOLD)));

        String message = trimToNull(analystInterpretation);
        if (message == null) {
            message = "No analyst interpretation was provided for this generated report.";
        }

        document.add(new Paragraph(message, font(10, Font.NORMAL)));
    }

    private PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        return table;
    }

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(headerCell(label));
        table.addCell(valueCell(value));
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(10, Font.BOLD)));
        cell.setPadding(6);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font(10, Font.NORMAL)));
        cell.setPadding(6);
        return cell;
    }

    private Font font(int size, int style) {
        return new Font(Font.HELVETICA, size, style);
    }

    private String periodLabel(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return "All available reports";
        }
        if (from != null && to != null) {
            return from + " to " + to;
        }
        if (from != null) {
            return "From " + from;
        }
        return "Until " + to;
    }

    private void validateAnalyticsPeriod(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();

        if ((from != null && from.isAfter(today)) || (to != null && to.isAfter(today))) {
            throw new IllegalArgumentException("Analytics period cannot be in the future.");
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
    }

    private AdverseEffectReportVersion createActiveVersion(DoctorReport report, User createdBy) {
        Integer maxVersionNumber = versionRepository.findMaxVersionNumberByReportId(report.getId());
        int nextVersionNumber = maxVersionNumber == null ? 1 : maxVersionNumber + 1;

        AdverseEffectReportVersion version = new AdverseEffectReportVersion();
        version.setReport(report);
        version.setVersionNumber(nextVersionNumber);
        version.setActive(true);
        version.setMedicationName(report.getMedicationName());
        version.setSource(report.getSource());
        version.setSeverity(report.getSeverity());
        version.setSymptomDate(report.getSymptomDate());
        version.setEffectDescription(report.getEffectDescription());
        version.setAdditionalNotes(report.getAdditionalNotes());
        version.setPatientGender(report.getPatientGender());
        version.setPatientAge(report.getPatientAge());
        version.setCreatedBy(createdBy);

        return versionRepository.save(version);
    }

    private void deactivateActiveVersions(Long reportId) {
        List<AdverseEffectReportVersion> activeVersions = versionRepository.findByReportIdAndActiveTrue(reportId);
        for (AdverseEffectReportVersion version : activeVersions) {
            version.setActive(false);
        }
        versionRepository.saveAll(activeVersions);
    }

    private AdverseEffectReportResponseDTO toDTO(AdverseEffectReport report) {
        AdverseEffectReportResponseDTO dto = new AdverseEffectReportResponseDTO();
        dto.setId(report.getId());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setSource(report.getSource());
        dto.setSeverity(report.getSeverity());
        dto.setSymptomDate(report.getSymptomDate());
        dto.setStatus(report.getStatus().name());
        dto.setMedicationName(report.getMedicationName());
        dto.setReporterUsername(report.getReporter().getUsername());
        if (report.getCurrentVersion() != null) {
            dto.setCurrentVersionId(report.getCurrentVersion().getId());
            dto.setCurrentVersionNumber(report.getCurrentVersion().getVersionNumber());
        }

        if (report instanceof DoctorReport dr) {
            dto.setReportType("DOCTOR");
            dto.setEffectDescription(dr.getEffectDescription());
            dto.setAdditionalNotes(dr.getAdditionalNotes());
            dto.setPatientGender(dr.getPatientGender());
            dto.setPatientAge(dr.getPatientAge());
        } else if (report instanceof PatientReport pr) {
            dto.setReportType("PATIENT");
            dto.setSymptoms(pr.getSymptoms());
            dto.setAdditionalDesc(pr.getAdditionalDesc());
            dto.setPatientGender(pr.getPatientGender());
            dto.setPatientAge(pr.getPatientAge());
            dto.setSymptomDate(pr.getSymptomDate());
        }

        return dto;
    }

    private AdverseEffectReportVersionResponseDTO toDTO(AdverseEffectReportVersion version) {
        AdverseEffectReportVersionResponseDTO dto = new AdverseEffectReportVersionResponseDTO();
        dto.setId(version.getId());
        dto.setReportId(version.getReport().getId());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setActive(version.isActive());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setCreatedByUsername(version.getCreatedBy().getUsername());
        dto.setMedicationName(version.getMedicationName());
        dto.setSource(version.getSource());
        dto.setSeverity(version.getSeverity());
        dto.setSymptomDate(version.getSymptomDate());
        dto.setEffectDescription(version.getEffectDescription());
        dto.setAdditionalNotes(version.getAdditionalNotes());
        dto.setPatientGender(version.getPatientGender());
        dto.setPatientAge(version.getPatientAge());
        return dto;
    }

    private StatusTransitionResponseDTO toDTO(StatusTransition transition) {
        StatusTransitionResponseDTO dto = new StatusTransitionResponseDTO();
        dto.setId(transition.getId());
        dto.setOldStatus(transition.getOldStatus());
        dto.setNewStatus(transition.getNewStatus());
        dto.setChangedAt(transition.getChangedAt());
        dto.setChangedByUsername(transition.getChangedBy().getUsername());
        dto.setComment(transition.getComment());
        dto.setPriority(transition.getPriority());
        dto.setClosureReason(transition.getClosureReason());
        dto.setVerdict(transition.getVerdict());
        return dto;
    }

    private AnalystNoteResponseDTO toDTO(AnalystNote note) {
        AnalystNoteResponseDTO dto = new AnalystNoteResponseDTO();
        dto.setId(note.getId());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setAuthorUsername(note.getAuthor().getUsername());
        return dto;
    }

    private boolean isWithinCreatedPeriod(AdverseEffectReport report, LocalDate from, LocalDate to) {
        LocalDate createdDate = report.getCreatedAt().toLocalDate();
        boolean afterStart = from == null || !createdDate.isBefore(from);
        boolean beforeEnd = to == null || !createdDate.isAfter(to);
        return afterStart && beforeEnd;
    }

    private long countByStatus(List<AdverseEffectReport> reports, ReportStatus status) {
        return reports.stream()
                .filter(report -> report.getStatus() == status)
                .count();
    }

    private Map<String, Long> countByMedication(List<AdverseEffectReport> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        report -> normalizeLabel(report.getMedicationName(), "Unknown medication"),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    private Map<String, Long> countByStatusLabel(List<AdverseEffectReport> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getStatus().name(),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    private Map<String, Long> countByReporterType(List<AdverseEffectReport> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        report -> report instanceof PatientReport ? "Patient" : "Doctor",
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    private Map<String, Long> countByEffect(List<AdverseEffectReport> reports) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (AdverseEffectReport report : reports) {
            for (String label : effectLabels(report)) {
                counts.merge(label, 1L, Long::sum);
            }
        }
        return counts;
    }

    private List<String> effectLabels(AdverseEffectReport report) {
        if (report instanceof PatientReport patientReport) {
            return splitSymptoms(patientReport.getSymptoms());
        }

        if (report instanceof DoctorReport doctorReport) {
            String description = normalizeLabel(doctorReport.getEffectDescription(), "Doctor free-text effect");
            return List.of(shorten(description, 70));
        }

        return List.of("Unspecified effect");
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

    private List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> toCountItems(Map<String, Long> counts, long totalReports) {
        return counts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                .map(entry -> new AdverseEffectAnalyticsSummaryDTO.CountItemDTO(
                        entry.getKey(),
                        entry.getValue(),
                        percentage(entry.getValue(), totalReports)))
                .collect(Collectors.toList());
    }

    private List<AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO> toTimeBuckets(List<AdverseEffectReport> reports) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return reports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getCreatedAt().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO(formatter.format(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    private double percentage(long count, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((count * 1000.0) / total) / 10.0;
    }

    private String normalizeLabel(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
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

    private AdverseEffectReport findReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private ReportStatus parseStatusOrNull(String status) {
        String normalized = normalizeFilter(status);
        return normalized == null ? null : parseRequiredStatus(normalized);
    }

    private ReportStatus parseRequiredStatus(String status) {
        try {
            return ReportStatus.valueOf(status.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unknown report status: " + status);
        }
    }

    private void validateTransition(ReportStatus oldStatus, ReportStatus newStatus, ChangeStatusRequestDTO dto) {
        if (oldStatus == ReportStatus.EVIDENCED) {
            throw new IllegalStateException("Patient reports are evidenced automatically and are not analyzed.");
        }

        boolean allowed =
                oldStatus == ReportStatus.SUBMITTED && newStatus == ReportStatus.UNDER_REVIEW
                || oldStatus == ReportStatus.UNDER_REVIEW && newStatus == ReportStatus.CLOSED;

        if (!allowed) {
            throw new IllegalStateException("Status transition not allowed");
        }

        if (newStatus == ReportStatus.CLOSED && trimToNull(dto.getComment()) == null) {
            throw new IllegalArgumentException("Comment is required when closing a report");
        }
    }

    private String normalizeFilter(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null || "ALL".equalsIgnoreCase(trimmed) ? null : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
