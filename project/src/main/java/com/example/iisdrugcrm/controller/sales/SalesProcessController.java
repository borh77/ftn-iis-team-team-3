package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.process.CreateSalesProcessRequestDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.StageUpdateRequestDTO;
import com.example.iisdrugcrm.service.sales.SalesProcessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/processes")
public class SalesProcessController {

    private final SalesProcessService salesProcessService;

    public SalesProcessController(SalesProcessService salesProcessService) {
        this.salesProcessService = salesProcessService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER')")
    public ResponseEntity<List<SalesProcessResponseDTO>> getAll() {
        return ResponseEntity.ok(salesProcessService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<SalesProcessResponseDTO> create(@Valid @RequestBody CreateSalesProcessRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesProcessService.create(dto));
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<SalesProcessResponseDTO> updateStage(
            @PathVariable Long id,
            @Valid @RequestBody StageUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(salesProcessService.updateStage(id, dto));
    }
}