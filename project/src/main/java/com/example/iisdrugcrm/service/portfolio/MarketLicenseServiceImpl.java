package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicense;
import com.example.iisdrugcrm.domain.portfolio.MarketLicenseStatus;
import com.example.iisdrugcrm.domain.portfolio.MarketProduct;
import com.example.iisdrugcrm.domain.portfolio.VariantVersion;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseStatusRequestDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.MarketLicenseRepository;
import com.example.iisdrugcrm.repository.portfolio.MarketProductRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseHistory;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseHistoryResponseDTO;
import com.example.iisdrugcrm.repository.portfolio.MarketLicenseHistoryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class MarketLicenseServiceImpl implements MarketLicenseService {

    private final MarketLicenseRepository marketLicenseRepository;
    private final MarketProductRepository marketProductRepository;
    private final VariantVersionRepository variantVersionRepository;
    private final MarketLicenseHistoryRepository marketLicenseHistoryRepository;

    public MarketLicenseServiceImpl(
            MarketLicenseRepository marketLicenseRepository,
            MarketProductRepository marketProductRepository,
            VariantVersionRepository variantVersionRepository,
            MarketLicenseHistoryRepository marketLicenseHistoryRepository
    ) {
        this.marketLicenseRepository = marketLicenseRepository;
        this.marketProductRepository = marketProductRepository;
        this.variantVersionRepository = variantVersionRepository;
        this.marketLicenseHistoryRepository = marketLicenseHistoryRepository;
    }

    @Override
    public List<MarketLicenseResponseDTO> getMarketLicenses(
            String search,
            Long marketProductId,
            Long variantVersionId,
            MarketLicenseStatus status
    ) {
        List<MarketLicense> licenses;

        if (search != null && !search.isBlank()) {
            licenses = marketLicenseRepository.searchByTextWithRelations(search.trim());
        } else if (marketProductId != null) {
            licenses = marketLicenseRepository.findByMarketProductIdWithRelations(marketProductId);
        } else if (variantVersionId != null) {
            licenses = marketLicenseRepository.findByVariantVersionIdWithRelations(variantVersionId);
        } else if (status != null) {
            licenses = marketLicenseRepository.findByStatusWithRelations(status);
        } else {
            licenses = marketLicenseRepository.findAllWithRelations();
        }

        return licenses.stream()
                .map(MarketLicenseResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public List<MarketLicenseResponseDTO> getLicensesExpiringUntil(LocalDate date) {
        return marketLicenseRepository.findLicensesExpiringUntil(date)
                .stream()
                .map(MarketLicenseResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public MarketLicenseResponseDTO create(MarketLicenseRequestDTO dto) {
        if (marketLicenseRepository.existsByLicenseNumberIgnoreCase(dto.getLicenseNumber())) {
            throw new PortfolioDuplicateResourceException("License number already exists");
        }

        if (marketLicenseRepository.existsByMarketProductIdAndVariantVersionId(
                dto.getMarketProductId(),
                dto.getVariantVersionId()
        )) {
            throw new PortfolioDuplicateResourceException(
                    "License already exists for this market product and variant version"
            );
        }

        MarketProduct marketProduct = getMarketProduct(dto.getMarketProductId());
        VariantVersion variantVersion = getVariantVersion(dto.getVariantVersionId());

        validateVersionBelongsToMarketProductVariant(marketProduct, variantVersion);

        MarketLicense license = new MarketLicense(
                marketProduct,
                variantVersion,
                normalize(dto.getLicenseNumber()),
                dto.getIssuedAt(),
                dto.getValidUntil()
        );

        return MarketLicenseResponseDTO.fromEntity(
                marketLicenseRepository.save(license)
        );
    }

    @Override
    @Transactional
    public MarketLicenseResponseDTO update(Long id, MarketLicenseRequestDTO dto) {
        MarketLicense license = getMarketLicense(id);

        if (!license.getMarketProduct().getId().equals(dto.getMarketProductId())) {
            throw new IllegalStateException(
                    "Market product cannot be changed for an existing license"
            );
        }

        if (!license.getVariantVersion().getId().equals(dto.getVariantVersionId())) {
            throw new IllegalStateException(
                    "Variant version cannot be changed for an existing license"
            );
        }

        if (marketLicenseRepository.existsByLicenseNumberIgnoreCaseAndIdNot(
                dto.getLicenseNumber(),
                id
        )) {
            throw new PortfolioDuplicateResourceException("License number already exists");
        }

        license.update(
                normalize(dto.getLicenseNumber()),
                dto.getIssuedAt(),
                dto.getValidUntil()
        );

        return MarketLicenseResponseDTO.fromEntity(
                marketLicenseRepository.save(license)
        );
    }

    @Override
    @Transactional
    public MarketLicenseResponseDTO changeStatus(
            Long id,
            MarketLicenseStatusRequestDTO dto
    ) {
        MarketLicense license = getMarketLicense(id);

        MarketLicenseStatus oldStatus = license.getStatus();
        MarketLicenseStatus newStatus = dto.getStatus();

        if (oldStatus == newStatus) {
            return MarketLicenseResponseDTO.fromEntity(license);
        }

        license.changeStatus(newStatus);

        marketLicenseHistoryRepository.save(
                new MarketLicenseHistory(
                        license,
                        oldStatus,
                        newStatus,
                        1L,
                        "Market license status changed"
                )
        );

        return MarketLicenseResponseDTO.fromEntity(
                marketLicenseRepository.save(license)
        );
    }

    private void validateVersionBelongsToMarketProductVariant(
            MarketProduct marketProduct,
            VariantVersion variantVersion
    ) {
        Long marketProductVariantId = marketProduct.getVariant().getId();
        Long variantVersionVariantId = variantVersion.getVariant().getId();

        if (!marketProductVariantId.equals(variantVersionVariantId)) {
            throw new IllegalStateException(
                    "Variant version must belong to the same variant as market product"
            );
        }
    }

    @Override
    public List<MarketLicenseHistoryResponseDTO> getHistory(Long marketLicenseId) {
        return marketLicenseHistoryRepository.findByMarketLicenseIdWithRelations(marketLicenseId)
                .stream()
                .map(MarketLicenseHistoryResponseDTO::fromEntity)
                .toList();
    }

    private MarketLicense getMarketLicense(Long id) {
        return marketLicenseRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Market license not found"));
    }

    private MarketProduct getMarketProduct(Long id) {
        return marketProductRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Market product not found"));
    }

    private VariantVersion getVariantVersion(Long id) {
        return variantVersionRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Variant version not found"));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}