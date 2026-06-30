package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.dto.pricelist.PricelistDashboardSummaryDTO;
import com.example.iisdrugcrm.service.PricelistDashboardService;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class PricelistDashboardController {

    private final PricelistDashboardService dashboardService;

    public PricelistDashboardController(PricelistDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/pricelist-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PricelistDashboardSummaryDTO> pricelistDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) PricelistStatus status,
            @RequestParam(required = false) String customerSegment
    ) {
        return ResponseEntity.ok(dashboardService.getSummary(
                null,
                true,
                dateFrom,
                dateTo,
                teamId,
                regionId,
                status,
                customerSegment
        ));
    }
}
