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
import com.example.iisdrugcrm.mongo.AdverseEffectMongoSyncService;
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
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

    private static final String REPORT_COMPREHENSIVE = "comprehensive";
    private static final String REPORT_MEDICATION = "medication";
    private static final String REPORT_EFFECTS = "effects";
    private static final String REPORT_STATUS = "status";
    private static final String REPORT_REPORTER = "reporter";
    private static final String REPORT_SOURCE = "source";
    private static final String REPORT_TIMELINE = "timeline";

    private static final Color COLOR_NAVY = new Color(21, 36, 71);
    private static final Color COLOR_BLUE = new Color(37, 99, 235);
    private static final Color COLOR_TEAL = new Color(15, 159, 143);
    private static final Color COLOR_GREEN = new Color(22, 163, 74);
    private static final Color COLOR_ORANGE = new Color(245, 158, 11);
    private static final Color COLOR_RED = new Color(220, 38, 38);
    private static final Color COLOR_PURPLE = new Color(124, 58, 237);
    private static final Color COLOR_GRAY = new Color(226, 232, 240);
    private static final Color COLOR_LIGHT = new Color(248, 250, 252);

    private final DoctorReportRepository doctorReportRepository;
    private final PatientReportRepository patientReportRepository;
    private final AdverseEffectReportRepository reportRepository;
    private final AdverseEffectReportVersionRepository versionRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final AnalystNoteRepository analystNoteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AdverseEffectMongoSyncService mongoSyncService;

    public AdverseEffectReportServiceImpl(
            DoctorReportRepository doctorReportRepository,
            PatientReportRepository patientReportRepository,
            AdverseEffectReportRepository reportRepository,
            AdverseEffectReportVersionRepository versionRepository,
            StatusTransitionRepository statusTransitionRepository,
            AnalystNoteRepository analystNoteRepository,
            UserRepository userRepository,
            EmailService emailService,
            AdverseEffectMongoSyncService mongoSyncService) {
        this.doctorReportRepository = doctorReportRepository;
        this.patientReportRepository = patientReportRepository;
        this.reportRepository = reportRepository;
        this.versionRepository = versionRepository;
        this.statusTransitionRepository = statusTransitionRepository;
        this.analystNoteRepository = analystNoteRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.mongoSyncService = mongoSyncService;
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

        AdverseEffectReportResponseDTO response = toDTO(doctorReportRepository.save(saved));
        mongoSyncService.syncReportSafely(saved.getId());
        return response;
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

        PatientReport saved = patientReportRepository.save(report);
        AdverseEffectReportResponseDTO response = toDTO(saved);
        mongoSyncService.syncReportSafely(saved.getId());
        return response;
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

        AdverseEffectReportResponseDTO response = toDTO(doctorReportRepository.save(saved));
        mongoSyncService.syncReportSafely(saved.getId());
        return response;
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

        AdverseEffectReportResponseDTO response = toDTO(saved);
        mongoSyncService.syncReportSafely(saved.getId());
        return response;
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

        AnalystNoteResponseDTO noteDto = toDTO(analystNoteRepository.save(note));
        mongoSyncService.syncReportSafely(reportId);
        return noteDto;
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
        summary.setReportsBySource(toCountItems(countBySource(reports), total));
        summary.setReportsOverTime(toTimeBuckets(reports));
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateAnalyticsPdfReport(LocalDate from, LocalDate to, String analystInterpretation, String reportType) {
        AdverseEffectAnalyticsSummaryDTO summary = getAnalyticsSummary(from, to);
        String normalizedReportType = normalizeReportType(reportType);
        if (REPORT_COMPREHENSIVE.equals(normalizedReportType) && trimToNull(analystInterpretation) == null) {
            throw new IllegalArgumentException("Analyst interpretation is required for the comprehensive analytics report.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 32, 32, 32, 32);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addReportHeader(document, normalizedReportType, from, to);
            addKpiSection(document, summary);
            addInsightSection(document, summary, analystInterpretation, normalizedReportType);
            addReportSections(document, summary, normalizedReportType);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate adverse effect analytics PDF report.", exception);
        }
    }

    private void addReportHeader(Document document, String reportType, LocalDate from, LocalDate to) throws Exception {
        PdfPTable header = table(1);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(16);
        cell.setBackgroundColor(COLOR_NAVY);

        Paragraph title = new Paragraph(reportTitle(reportType), font(18, Font.BOLD, Color.WHITE));
        title.setSpacingAfter(6);
        cell.addElement(title);
        cell.addElement(new Paragraph("Adverse effect reports | Period: " + periodLabel(from, to), font(10, Font.NORMAL, Color.WHITE)));
        cell.addElement(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), font(9, Font.NORMAL, new Color(207, 217, 235))));
        header.addCell(cell);
        document.add(header);
        document.add(new Paragraph(" "));
    }

    private void addKpiSection(Document document, AdverseEffectAnalyticsSummaryDTO summary) throws Exception {
        PdfPTable kpis = table(3);
        kpis.setSpacingBefore(4);
        kpis.addCell(metricCell("Total cases", summary.getTotalReports(), "All adverse effect reports", COLOR_BLUE));
        kpis.addCell(metricCell("Doctor reports", summary.getDoctorReports(), percentLabel(summary.getDoctorReports(), summary.getTotalReports()), COLOR_TEAL));
        kpis.addCell(metricCell("Patient reports", summary.getPatientReports(), percentLabel(summary.getPatientReports(), summary.getTotalReports()), COLOR_PURPLE));
        kpis.addCell(metricCell("Submitted", summary.getSubmittedReports(), "Awaiting review", COLOR_ORANGE));
        kpis.addCell(metricCell("Under review", summary.getUnderReviewReports(), "Analyst workload", COLOR_RED));
        kpis.addCell(metricCell("Closed", summary.getClosedReports(), "Completed analysis", COLOR_GREEN));
        document.add(kpis);
        document.add(new Paragraph(" "));
    }

    private PdfPCell metricCell(String label, long value, String note, Color accent) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(COLOR_GRAY);
        cell.setBackgroundColor(COLOR_LIGHT);

        Paragraph labelParagraph = new Paragraph(label.toUpperCase(Locale.ROOT), font(8, Font.BOLD, new Color(88, 103, 128)));
        labelParagraph.setSpacingAfter(4);
        cell.addElement(labelParagraph);
        cell.addElement(new Paragraph(String.valueOf(value), font(20, Font.BOLD, accent)));
        cell.addElement(new Paragraph(note == null ? "" : note, font(8, Font.NORMAL, new Color(95, 111, 136))));
        return cell;
    }

    private void addInsightSection(Document document, AdverseEffectAnalyticsSummaryDTO summary, String analystInterpretation, String reportType) throws Exception {
        PdfPTable table = table(1);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(new Color(191, 219, 254));
        cell.setBackgroundColor(new Color(239, 246, 255));
        cell.addElement(new Paragraph("Executive insight", font(12, Font.BOLD, COLOR_NAVY)));
        cell.addElement(new Paragraph(autoInsight(summary, reportType), font(10, Font.NORMAL, new Color(46, 61, 86))));

        String analystText = trimToNull(analystInterpretation);
        if (analystText != null) {
            Paragraph analyst = new Paragraph("Analyst interpretation: " + analystText, font(10, Font.BOLD, new Color(31, 75, 140)));
            analyst.setSpacingBefore(8);
            cell.addElement(analyst);
        }

        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addReportSections(Document document, AdverseEffectAnalyticsSummaryDTO summary, String reportType) throws Exception {
        if (REPORT_COMPREHENSIVE.equals(reportType) || REPORT_STATUS.equals(reportType)) {
            addCountChartSection(document, "Workflow status pipeline", "Shows operational workload across Submitted, Under Review, Closed and Evidenced reports.", summary.getReportsByStatus(), COLOR_ORANGE);
        }
        if (REPORT_COMPREHENSIVE.equals(reportType) || REPORT_MEDICATION.equals(reportType)) {
            addCountChartSection(document, "Medicine signal ranking", "Highlights medicines with the highest adverse-effect report volume.", summary.getReportsByMedication(), COLOR_BLUE);
        }
        if (REPORT_COMPREHENSIVE.equals(reportType) || REPORT_EFFECTS.equals(reportType)) {
            addCountChartSection(document, "Adverse effect profile", "Ranks the most frequent recorded symptoms or effect descriptions.", summary.getReportsByEffect(), COLOR_RED);
        }
        if (REPORT_COMPREHENSIVE.equals(reportType) || REPORT_REPORTER.equals(reportType)) {
            addCountChartSection(document, "Reporter contribution", "Compares reports submitted by doctors and patients.", summary.getReportsByReporterType(), COLOR_TEAL);
        }
        if (REPORT_COMPREHENSIVE.equals(reportType) || REPORT_SOURCE.equals(reportType)) {
            addCountChartSection(document, "Submission channel breakdown", "Shows whether cases came through web, patient portal or another configured source.", summary.getReportsBySource(), COLOR_PURPLE);
        }
        if (REPORT_COMPREHENSIVE.equals(reportType) || REPORT_TIMELINE.equals(reportType)) {
            addTimelineSection(document, "Case intake over time", "Daily report volume used for spotting spikes in pharmacovigilance workload.", summary.getReportsOverTime(), COLOR_GREEN);
        }
    }

    private void addCountChartSection(
            Document document,
            String title,
            String description,
            List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> items,
            Color color) throws Exception {
        addSectionHeading(document, title, description);

        if (items == null || items.isEmpty()) {
            document.add(emptyParagraph("No data available for this statistic."));
            return;
        }

        PdfPTable chart = table(4);
        chart.setWidths(new float[]{2.4f, 4.2f, 1f, 1f});
        chart.addCell(headerCell("Item"));
        chart.addCell(headerCell("Distribution"));
        chart.addCell(headerCell("Count"));
        chart.addCell(headerCell("Share"));

        for (AdverseEffectAnalyticsSummaryDTO.CountItemDTO item : items.stream().limit(12).collect(Collectors.toList())) {
            chart.addCell(valueCell(item.getLabel()));
            chart.addCell(barChartCell(item.getPercentage(), color));
            chart.addCell(valueCell(String.valueOf(item.getCount())));
            chart.addCell(valueCell(item.getPercentage() + "%"));
        }

        document.add(chart);
        addTopFinding(document, items);
        document.add(new Paragraph(" "));
    }

    private void addTimelineSection(
            Document document,
            String title,
            String description,
            List<AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO> items,
            Color color) throws Exception {
        addSectionHeading(document, title, description);

        if (items == null || items.isEmpty()) {
            document.add(emptyParagraph("No timeline data available."));
            return;
        }

        long max = items.stream().mapToLong(AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO::getCount).max().orElse(0);
        PdfPTable chart = table(3);
        chart.setWidths(new float[]{2f, 5f, 1f});
        chart.addCell(headerCell("Date"));
        chart.addCell(headerCell("Volume"));
        chart.addCell(headerCell("Count"));

        for (AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO item : items.stream().limit(14).collect(Collectors.toList())) {
            double percentage = max == 0 ? 0 : Math.round((item.getCount() * 1000.0) / max) / 10.0;
            chart.addCell(valueCell(item.getPeriod()));
            chart.addCell(barChartCell(percentage, color));
            chart.addCell(valueCell(String.valueOf(item.getCount())));
        }

        document.add(chart);
        document.add(new Paragraph(" "));
    }

    private void addSectionHeading(Document document, String title, String description) throws Exception {
        Paragraph heading = new Paragraph(title, font(13, Font.BOLD, COLOR_NAVY));
        heading.setSpacingBefore(8);
        heading.setSpacingAfter(4);
        document.add(heading);
        document.add(new Paragraph(description, font(9, Font.NORMAL, new Color(90, 102, 122))));
    }

    private void addTopFinding(Document document, List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> items) throws Exception {
        AdverseEffectAnalyticsSummaryDTO.CountItemDTO top = items.isEmpty() ? null : items.get(0);
        if (top == null) {
            return;
        }
        Paragraph finding = new Paragraph(
                "Top finding: " + top.getLabel() + " accounts for " + top.getCount() + " reports (" + top.getPercentage() + "%).",
                font(9, Font.BOLD, new Color(42, 68, 112)));
        finding.setSpacingBefore(6);
        document.add(finding);
    }

    private PdfPCell barChartCell(double percentage, Color color) {
        try {
            float filled = (float) Math.max(2, Math.min(99, percentage));
            float empty = Math.max(1, 100 - filled);

            PdfPTable bar = new PdfPTable(2);
            bar.setWidthPercentage(100);
            bar.setWidths(new float[]{filled, empty});

            PdfPCell filledCell = new PdfPCell(new Phrase(" "));
            filledCell.setFixedHeight(10);
            filledCell.setBorder(PdfPCell.NO_BORDER);
            filledCell.setBackgroundColor(color);
            bar.addCell(filledCell);

            PdfPCell emptyCell = new PdfPCell(new Phrase(" "));
            emptyCell.setFixedHeight(10);
            emptyCell.setBorder(PdfPCell.NO_BORDER);
            emptyCell.setBackgroundColor(COLOR_GRAY);
            bar.addCell(emptyCell);

            PdfPCell wrapper = new PdfPCell(bar);
            wrapper.setPadding(7);
            wrapper.setBorderColor(COLOR_GRAY);
            return wrapper;
        } catch (Exception exception) {
            return valueCell(percentage + "%");
        }
    }

    private PdfPTable table(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8);
        return table;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(9, Font.BOLD, Color.WHITE)));
        cell.setPadding(7);
        cell.setBorderColor(COLOR_NAVY);
        cell.setBackgroundColor(COLOR_NAVY);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font(9, Font.NORMAL, new Color(38, 50, 69))));
        cell.setPadding(7);
        cell.setBorderColor(COLOR_GRAY);
        return cell;
    }

    private Paragraph emptyParagraph(String text) {
        Paragraph paragraph = new Paragraph(text, font(9, Font.NORMAL, new Color(95, 111, 136)));
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    private Font font(int size, int style) {
        return font(size, style, Color.BLACK);
    }

    private Font font(int size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private String reportTitle(String reportType) {
        return switch (reportType) {
            case REPORT_MEDICATION -> "Medicine Signal Ranking Report";
            case REPORT_EFFECTS -> "Adverse Effect Profile Report";
            case REPORT_STATUS -> "Workflow Status Report";
            case REPORT_REPORTER -> "Reporter Contribution Report";
            case REPORT_SOURCE -> "Submission Channel Report";
            case REPORT_TIMELINE -> "Case Intake Trend Report";
            default -> "Comprehensive Pharmacovigilance Analytics Report";
        };
    }

    private String autoInsight(AdverseEffectAnalyticsSummaryDTO summary, String reportType) {
        AdverseEffectAnalyticsSummaryDTO.CountItemDTO medication = firstItem(summary.getReportsByMedication());
        AdverseEffectAnalyticsSummaryDTO.CountItemDTO effect = firstItem(summary.getReportsByEffect());

        if (summary.getTotalReports() == 0) {
            return "No adverse effect reports are available yet. The report is ready to populate once cases are submitted.";
        }

        String lead = medication == null
                ? "No medicine has enough report volume to form a clear signal."
                : medication.getLabel() + " has the highest case volume with " + medication.getCount() + " reports.";
        String effectText = effect == null
                ? "Effect distribution is not available yet."
                : effect.getLabel() + " is the most frequent recorded effect.";
        String workload = summary.getUnderReviewReports() + " reports are currently under review and " + summary.getClosedReports() + " are closed.";

        if (REPORT_STATUS.equals(reportType)) {
            return workload + " Submitted reports should be triaged first to reduce review backlog.";
        }
        if (REPORT_EFFECTS.equals(reportType)) {
            return effectText + " Compare this with the leading medicine signal before drawing a causality conclusion.";
        }
        if (REPORT_TIMELINE.equals(reportType)) {
            return "Timeline view helps identify intake spikes. " + lead;
        }
        return lead + " " + effectText + " " + workload;
    }

    private AdverseEffectAnalyticsSummaryDTO.CountItemDTO firstItem(List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> items) {
        return items == null || items.isEmpty() ? null : items.get(0);
    }

    private String normalizeReportType(String reportType) {
        String normalized = trimToNull(reportType);
        if (normalized == null) {
            return REPORT_COMPREHENSIVE;
        }

        normalized = normalized.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case REPORT_MEDICATION, REPORT_EFFECTS, REPORT_STATUS, REPORT_REPORTER, REPORT_SOURCE, REPORT_TIMELINE -> normalized;
            default -> REPORT_COMPREHENSIVE;
        };
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

    private String percentLabel(long part, long total) {
        return percentage(part, total) + "% of dataset";
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

    private Map<String, Long> countBySource(List<AdverseEffectReport> reports) {
        return reports.stream()
                .collect(Collectors.groupingBy(
                        this::sourceLabel,
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
