package com.example.iisdrugcrm.service.adverse;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.adverse.AdverseEffectReport;
import com.example.iisdrugcrm.domain.adverse.AnalystNote;
import com.example.iisdrugcrm.domain.adverse.DoctorReport;
import com.example.iisdrugcrm.domain.adverse.PatientReport;
import com.example.iisdrugcrm.domain.adverse.ReportStatus;
import com.example.iisdrugcrm.domain.adverse.StatusTransition;
import com.example.iisdrugcrm.dto.adverse.AddNoteRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AnalystNoteResponseDTO;
import com.example.iisdrugcrm.dto.adverse.ChangeStatusRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.StatusTransitionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.UpdateDoctorReportRequestDTO;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.repository.adverse.AdverseEffectReportRepository;
import com.example.iisdrugcrm.repository.adverse.AnalystNoteRepository;
import com.example.iisdrugcrm.repository.adverse.DoctorReportRepository;
import com.example.iisdrugcrm.repository.adverse.PatientReportRepository;
import com.example.iisdrugcrm.repository.adverse.StatusTransitionRepository;
import com.example.iisdrugcrm.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdverseEffectReportServiceImpl implements AdverseEffectReportService {

    private final DoctorReportRepository doctorReportRepository;
    private final PatientReportRepository patientReportRepository;
    private final AdverseEffectReportRepository reportRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final AnalystNoteRepository analystNoteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AdverseEffectReportServiceImpl(
            DoctorReportRepository doctorReportRepository,
            PatientReportRepository patientReportRepository,
            AdverseEffectReportRepository reportRepository,
            StatusTransitionRepository statusTransitionRepository,
            AnalystNoteRepository analystNoteRepository,
            UserRepository userRepository,
            EmailService emailService) {
        this.doctorReportRepository = doctorReportRepository;
        this.patientReportRepository = patientReportRepository;
        this.reportRepository = reportRepository;
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

        return toDTO(doctorReportRepository.save(report));
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

        return toDTO(doctorReportRepository.save(report));
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
