package com.example.iisdrugcrm.controller.adverse;

import com.example.iisdrugcrm.dto.adverse.AddNoteRequestDTO;
import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.AnalystNoteResponseDTO;
import com.example.iisdrugcrm.dto.adverse.ChangeStatusRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.StatusTransitionResponseDTO;
import com.example.iisdrugcrm.dto.adverse.UpdateDoctorReportRequestDTO;
import com.example.iisdrugcrm.service.adverse.AdverseEffectReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adverse-effects")
public class AdverseEffectReportController {

    private final AdverseEffectReportService service;

    public AdverseEffectReportController(AdverseEffectReportService service) {
        this.service = service;
    }

    // US-01: Lekar kreira nalog
    @PostMapping("/doctor-reports")
    @PreAuthorize("hasRole('LEKAR')")
    public ResponseEntity<AdverseEffectReportResponseDTO> createDoctorReport(
            @Valid @RequestBody CreateDoctorReportRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createDoctorReport(dto, auth.getName()));
    }

    // US-02: Pacijent kreira nalog
    @PostMapping("/patient-reports")
    @PreAuthorize("hasRole('PACIJENT')")
    public ResponseEntity<AdverseEffectReportResponseDTO> createPatientReport(
            @Valid @RequestBody CreatePatientReportRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPatientReport(dto, auth.getName()));
    }

    // US-03: Lekar vidi samo svoje naloge
    @GetMapping("/my-reports")
    @PreAuthorize("hasRole('LEKAR')")
    public ResponseEntity<List<AdverseEffectReportResponseDTO>> getMyReports(Authentication auth) {
        return ResponseEntity.ok(service.getMyReports(auth.getName()));
    }

    // US-04: Farmakovigilant vidi sve naloge
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

    // US-03: Detalji jednog naloga
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

    // US-03: Editovanje naloga lekara (samo dok je SUBMITTED)
    @PutMapping("/doctor-reports/{id}")
    @PreAuthorize("hasRole('LEKAR')")
    public ResponseEntity<AdverseEffectReportResponseDTO> updateDoctorReport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorReportRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(service.updateDoctorReport(id, dto, auth.getName()));
    }
}
