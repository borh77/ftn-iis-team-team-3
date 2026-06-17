package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.activity.ActivityRequestDTO;
import com.example.iisdrugcrm.dto.sales.activity.ActivityResponseDTO;
import com.example.iisdrugcrm.service.sales.SalesActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesActivityController {

    private final SalesActivityService salesActivityService;

    public SalesActivityController(SalesActivityService salesActivityService) {
        this.salesActivityService = salesActivityService;
    }

    @PostMapping("/processes/{id}/activities")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<ActivityResponseDTO> create(
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salesActivityService.create(id, dto));
    }

    @GetMapping("/processes/{id}/activities")
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public ResponseEntity<List<ActivityResponseDTO>> getByProcess(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(salesActivityService.getByProcess(id));
    }

    @PatchMapping("/activities/{id}/complete")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<ActivityResponseDTO> complete(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(salesActivityService.complete(id));
    }
}