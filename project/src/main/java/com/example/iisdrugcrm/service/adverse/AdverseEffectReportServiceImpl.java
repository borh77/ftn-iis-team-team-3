package com.example.iisdrugcrm.service.adverse;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.adverse.*;
import com.example.iisdrugcrm.dto.adverse.*;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.repository.adverse.AdverseEffectReportRepository;
import com.example.iisdrugcrm.repository.adverse.DoctorReportRepository;
import com.example.iisdrugcrm.repository.adverse.PatientReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdverseEffectReportServiceImpl implements AdverseEffectReportService {

    private final DoctorReportRepository doctorReportRepository;
    private final PatientReportRepository patientReportRepository;
    private final AdverseEffectReportRepository reportRepository;
    private final UserRepository userRepository;

    public AdverseEffectReportServiceImpl(
            DoctorReportRepository doctorReportRepository,
            PatientReportRepository patientReportRepository,
            AdverseEffectReportRepository reportRepository,
            UserRepository userRepository) {
        this.doctorReportRepository = doctorReportRepository;
        this.patientReportRepository = patientReportRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
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
        report.setReporter(reporter);
        report.setStatus(ReportStatus.SUBMITTED); // US-01: uvek počinje kao SUBMITTED

        DoctorReport saved = doctorReportRepository.save(report);
        return toDTO(saved);
    }

    @Override
    public AdverseEffectReportResponseDTO createPatientReport(CreatePatientReportRequestDTO dto, String username) {
        User reporter = findUserByUsername(username);

        PatientReport report = new PatientReport();
        report.setMedicationName(dto.getMedicationName());
        report.setSymptoms(dto.getSymptoms());
        report.setAdditionalDesc(dto.getAdditionalDesc());
        report.setReporter(reporter);
        report.setStatus(ReportStatus.EVIDENCED); // US-02: pacijentov nalog odmah EVIDENCED

        PatientReport saved = patientReportRepository.save(report);
        return toDTO(saved);
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
                .orElseThrow(() -> new RuntimeException("Nalog nije pronađen: " + id));

        // US-03: editovanje dozvoljeno SAMO dok je status SUBMITTED
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new IllegalStateException("Nalog se može menjati samo dok je u statusu SUBMITTED.");
        }

        // Provera da lekar može da menja samo SVOJE naloge
        if (!report.getReporter().getUsername().equals(username)) {
            throw new IllegalStateException("Nemate pravo da menjate tuđi nalog.");
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
        return reportRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdverseEffectReportResponseDTO getReportById(Long id) {
        AdverseEffectReport report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nalog nije pronađen: " + id));
        return toDTO(report);
    }

    // Pomoćna metoda — konvertuje entitet u DTO
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
        } else if (report instanceof PatientReport pr) {
            dto.setReportType("PATIENT");
            dto.setSymptoms(pr.getSymptoms());
            dto.setAdditionalDesc(pr.getAdditionalDesc());
        }

        return dto;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen: " + username));
    }
}
