package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseStatus;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseHistoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseStatusRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseHistoryResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface MarketLicenseService {

    List<MarketLicenseResponseDTO> getMarketLicenses(
            String search,
            Long marketProductId,
            Long variantVersionId,
            MarketLicenseStatus status
    );

    List<MarketLicenseResponseDTO> getLicensesExpiringUntil(LocalDate date);

    MarketLicenseResponseDTO create(MarketLicenseRequestDTO dto);

    MarketLicenseResponseDTO update(Long id, MarketLicenseRequestDTO dto);

    MarketLicenseResponseDTO changeStatus(Long id, MarketLicenseStatusRequestDTO dto);

    List<MarketLicenseHistoryResponseDTO> getHistory(Long marketLicenseId);
}