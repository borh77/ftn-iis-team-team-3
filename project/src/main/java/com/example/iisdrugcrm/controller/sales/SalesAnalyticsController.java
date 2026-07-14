package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.analytics.SalesAnalyticsSummaryDTO;
import com.example.iisdrugcrm.service.sales.SalesAnalyticsService;
import com.example.iisdrugcrm.dto.sales.analytics.SalesStagnationAlertDTO;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/analytics")
public class SalesAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;

    public SalesAnalyticsController(SalesAnalyticsService salesAnalyticsService) {
        this.salesAnalyticsService = salesAnalyticsService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public SalesAnalyticsSummaryDTO getSummary() {
        return salesAnalyticsService.getSummary();
    }

    @GetMapping(value = "/report", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public ResponseEntity<byte[]> exportReport() {
        byte[] pdf = salesAnalyticsService.generatePdfReport();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=sales-analytics-report.pdf")
                .body(pdf);
    }

    @PostMapping("/stagnation-check")
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public ResponseEntity<Void> runStagnationCheck() {
        salesAnalyticsService.runStagnationCheck();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stagnation-alerts")
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public List<SalesStagnationAlertDTO> getStagnationAlerts() {
        return salesAnalyticsService.getOpenStagnationAlerts();
    }
}