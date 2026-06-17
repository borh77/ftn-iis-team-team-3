package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusCountDTO;
import com.example.iisdrugcrm.service.portfolio.PortfolioAnalyticsService;
import org.springframework.web.bind.annotation.*;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio-analytics")
public class PortfolioAnalyticsController {

    private final PortfolioAnalyticsService portfolioAnalyticsService;

    public PortfolioAnalyticsController(
            PortfolioAnalyticsService portfolioAnalyticsService
    ) {
        this.portfolioAnalyticsService = portfolioAnalyticsService;
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
}