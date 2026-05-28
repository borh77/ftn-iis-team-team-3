package com.example.iisdrugcrm.controller.adverse;

import com.example.iisdrugcrm.dto.adverse.AdverseEffectReportResponseDTO;
import com.example.iisdrugcrm.dto.adverse.CreateDoctorReportRequestDTO;
import com.example.iisdrugcrm.dto.adverse.CreatePatientReportRequestDTO;
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
    public ResponseEntity<List<AdverseEffectReportResponseDTO>> getAllReports() {
        return ResponseEntity.ok(service.getAllReports());
    }

    // US-03: Detalji jednog naloga
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEKAR', 'FARMAKOVIGILANT')")
    public ResponseEntity<AdverseEffectReportResponseDTO> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReportById(id));
    }
}
