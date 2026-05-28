package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.lead.LeadRequestDTO;
import com.example.iisdrugcrm.dto.sales.lead.LeadResponseDTO;
import com.example.iisdrugcrm.service.sales.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.iisdrugcrm.dto.sales.customer.CustomerResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/sales/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public ResponseEntity<List<LeadResponseDTO>> getAll() {
        return ResponseEntity.ok(leadService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<LeadResponseDTO> create(@Valid @RequestBody LeadRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<LeadResponseDTO> update(@PathVariable Long id, @Valid @RequestBody LeadRequestDTO dto) {
        return ResponseEntity.ok(leadService.update(id, dto));
    }

    @PatchMapping("/{id}/qualify")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<LeadResponseDTO> qualify(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.qualify(id));
    }

    @PatchMapping("/{id}/convert")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<CustomerResponseDTO> convert(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.convert(id));
    }
}