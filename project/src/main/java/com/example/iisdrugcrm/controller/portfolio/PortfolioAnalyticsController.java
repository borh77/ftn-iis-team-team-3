package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusCountDTO;
import com.example.iisdrugcrm.service.portfolio.PortfolioAnalyticsService;
import org.springframework.web.bind.annotation.*;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO;

import com.example.iisdrugcrm.dto.portfolio.MarketLicenseStatusCountDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketProductCountByRegionDTO;

import com.example.iisdrugcrm.service.portfolio.PortfolioReportPdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio-analytics")
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService portfolioAnalyticsService;
    private final PortfolioReportPdfService portfolioReportPdfService;

    public PortfolioAnalyticsController(
        PortfolioAnalyticsService portfolioAnalyticsService,
        PortfolioReportPdfService portfolioReportPdfService
    ) {
        this.portfolioAnalyticsService = portfolioAnalyticsService;
        this.portfolioReportPdfService = portfolioReportPdfService;
    }

    @GetMapping("/variant-version-status-count")
    public List<VariantVersionStatusCountDTO> getVariantVersionStatusCount() {
        return portfolioAnalyticsService.getVariantVersionStatusCount();
    }

    @GetMapping("/variants/{variantId}/lifecycle-history")
    public List<VariantVersionLifecycleHistoryResponseDTO>
    getVariantLifecycleHistory(
            @PathVariable Long variantId
    ) {
        return portfolioAnalyticsService.getVariantLifecycleHistory(
                variantId
        );
    }

    @GetMapping("/products/by-therapeutic-area")
    public List<ProductCountByTherapeuticAreaDTO> getActiveProductCountByTherapeuticArea() {
        return portfolioAnalyticsService.getActiveProductCountByTherapeuticArea();
    }

    @GetMapping("/market-license-status-count")
    public List<MarketLicenseStatusCountDTO> getMarketLicenseStatusCount() {
        return portfolioAnalyticsService.getMarketLicenseStatusCount();
    }

    @GetMapping("/market-products/by-region")
    public List<MarketProductCountByRegionDTO> getActiveMarketProductCountByRegion() {
        return portfolioAnalyticsService.getActiveMarketProductCountByRegion();
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> downloadPortfolioAnalyticsReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate expiringUntil
    ) {

        byte[] pdf =
                portfolioReportPdfService.generateAnalyticsReportPdf(expiringUntil);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("portfolio-analytics-report.pdf")
                                .build()
                                .toString()
                )
                .body(pdf);
    }

}