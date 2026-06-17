package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseStatus;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseStatusRequestDTO;
import com.example.iisdrugcrm.service.portfolio.MarketLicenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.iisdrugcrm.dto.portfolio.MarketLicenseHistoryResponseDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/market-licenses")
public class MarketLicenseController {

    private final MarketLicenseService marketLicenseService;

    public MarketLicenseController(MarketLicenseService marketLicenseService) {
        this.marketLicenseService = marketLicenseService;
    }

    @GetMapping
    public List<MarketLicenseResponseDTO> getMarketLicenses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long marketProductId,
            @RequestParam(required = false) Long variantVersionId,
            @RequestParam(required = false) MarketLicenseStatus status
    ) {
        return marketLicenseService.getMarketLicenses(
                search,
                marketProductId,
                variantVersionId,
                status
        );
    }

    @GetMapping("/expiring")
    public List<MarketLicenseResponseDTO> getLicensesExpiringUntil(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return marketLicenseService.getLicensesExpiringUntil(date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public MarketLicenseResponseDTO create(
            @Valid @RequestBody MarketLicenseRequestDTO dto
    ) {
        return marketLicenseService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public MarketLicenseResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody MarketLicenseRequestDTO dto
    ) {
        return marketLicenseService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public MarketLicenseResponseDTO changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody MarketLicenseStatusRequestDTO dto
    ) {
        return marketLicenseService.changeStatus(id, dto);
    }

    @GetMapping("/{id}/history")
    public List<MarketLicenseHistoryResponseDTO> getHistory(@PathVariable Long id) {
        return marketLicenseService.getHistory(id);
    }
}