package com.example.iisdrugcrm.controller.adverse;

import com.example.iisdrugcrm.dto.adverse.AddNoteRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectAnalyticsPdfRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectAnalyticsSummaryDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportVersionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AnalystNoteResponseDTO;
import com.example.iisdrugcrm.dto.adverse.ChangeStatusRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.StatusTransitionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.UpdateDoctorReportRequestDTO;
import com.example.iisdrugcrm.service.adverse.AdverseEffectReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/adverse-effects")
public class AdverseEffectReportController {

    private final AdverseEffectReportService service;

    public AdverseEffectReportController(AdverseEffectReportService service) {
        this.service = service;
    }

    // US-01: Doctor creates a report
    @PostMapping("/doctor-reports")
    @PreAuthorize("hasRole('LEKAR')")
    public ResponseEntity<AdverseEffectReportResponseDTO> createDoctorReport(
            @Valid @RequestBody CreateDoctorReportRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createDoctorReport(dto, auth.getName()));
    }

    // US-02: Patient creates a report
    @PostMapping("/patient-reports")
    @PreAuthorize("hasRole('PACIJENT')")
    public ResponseEntity<AdverseEffectReportResponseDTO> createPatientReport(
            @Valid @RequestBody CreatePatientReportRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPatientReport(dto, auth.getName()));
    }

    // US-03: Doctor sees only their reports
    @GetMapping("/my-reports")
    @PreAuthorize("hasRole('LEKAR')")
    public ResponseEntity<List<AdverseEffectReportResponseDTO>> getMyReports(Authentication auth) {
        return ResponseEntity.ok(service.getMyReports(auth.getName()));
    }

    // US-04: Pharmacovigilance user sees all reports
    @GetMapping
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<List<AdverseEffectReportResponseDTO>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String medicationName,
            @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(service.getAllReportsFiltered(status, medicationName, severity));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<List<AdverseEffectReportResponseDTO>> getAllReportsFiltered(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String medicationName,
            @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(service.getAllReportsFiltered(status, medicationName, severity));
    }

    @GetMapping("/analytics/summary")
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<AdverseEffectAnalyticsSummaryDTO> getAnalyticsSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.getAnalyticsSummary(from, to));
    }

    @GetMapping("/analytics/report/pdf")
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<byte[]> getAnalyticsPdfReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String reportType) {
        byte[] pdf = service.generateAnalyticsPdfReport(from, to, null, reportType);

        return pdfResponse(pdf, reportType);
    }

    @PostMapping("/analytics/report/pdf")
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<byte[]> createAnalyticsPdfReport(
            @RequestBody(required = false) AdverseEffectAnalyticsPdfRequestDTO request) {
        LocalDate from = request == null ? null : request.getFrom();
        LocalDate to = request == null ? null : request.getTo();
        String analystInterpretation = request == null ? null : request.getAnalystInterpretation();
        String reportType = request == null ? null : request.getReportType();
        byte[] pdf = service.generateAnalyticsPdfReport(from, to, analystInterpretation, reportType);

        return pdfResponse(pdf, reportType);
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String reportType) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("adverse-effect-" + normalizeReportType(reportType) + "-report.pdf")
                        .build()
                        .toString())
                .body(pdf);
    }

    private String normalizeReportType(String reportType) {
        if (reportType == null || reportType.isBlank()) {
            return "comprehensive";
        }
        return reportType.trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");
    }

    // US-03: Single report details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEKAR', 'FARMAKOVIGILANT')")
    public ResponseEntity<AdverseEffectReportResponseDTO> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReportById(id));
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("hasAnyRole('LEKAR', 'FARMAKOVIGILANT')")
    public ResponseEntity<AdverseEffectReportResponseDTO> getReportByIdAlias(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReportById(id));
    }

    @PutMapping("/reports/{id}/status")
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<AdverseEffectReportResponseDTO> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(service.changeStatus(id, dto, auth.getName()));
    }

    @PostMapping("/reports/{id}/notes")
    @PreAuthorize("hasRole('FARMAKOVIGILANT')")
    public ResponseEntity<AnalystNoteResponseDTO> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addNote(id, dto, auth.getName()));
    }

    @GetMapping("/reports/{id}/history")
    @PreAuthorize("hasAnyRole('FARMAKOVIGILANT','LEKAR')")
    public ResponseEntity<List<StatusTransitionResponseDTO>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStatusHistory(id));
    }

    @GetMapping("/reports/{id}/notes")
    @PreAuthorize("hasAnyRole('FARMAKOVIGILANT','LEKAR')")
    public ResponseEntity<List<AnalystNoteResponseDTO>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(service.getNotes(id));
    }

    @GetMapping("/reports/{id}/versions")
    @PreAuthorize("hasAnyRole('FARMAKOVIGILANT','LEKAR')")
    public ResponseEntity<List<AdverseEffectReportVersionResponseDTO>> getVersions(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(service.getReportVersions(id, auth.getName()));
    }

    // US-03: Doctor report editing only while SUBMITTED
    @PutMapping("/doctor-reports/{id}")
    @PreAuthorize("hasRole('LEKAR')")
    public ResponseEntity<AdverseEffectReportResponseDTO> updateDoctorReport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorReportRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(service.updateDoctorReport(id, dto, auth.getName()));
    }
}
