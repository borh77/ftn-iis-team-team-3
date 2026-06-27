package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusCountDTO;
import com.example.iisdrugcrm.repository.portfolio.ProductRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionLifecycleHistoryRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionRepository;
import org.springframework.stereotype.Service;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.dto.portfolio.MarketLicenseStatusCountDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketProductCountByRegionDTO;
import com.example.iisdrugcrm.repository.portfolio.MarketLicenseRepository;
import com.example.iisdrugcrm.repository.portfolio.MarketProductRepository;


import java.util.List;

@Service
public class PortfolioAnalyticsServiceImpl implements PortfolioAnalyticsService {

    private final VariantVersionRepository variantVersionRepository;
    private final VariantVersionLifecycleHistoryRepository lifecycleHistoryRepository;

    private final ProductRepository productRepository;

    private final MarketLicenseRepository marketLicenseRepository;
    private final MarketProductRepository marketProductRepository;

    public PortfolioAnalyticsServiceImpl(
        VariantVersionRepository variantVersionRepository,
        VariantVersionLifecycleHistoryRepository lifecycleHistoryRepository,
        ProductRepository productRepository,
        MarketLicenseRepository marketLicenseRepository,
        MarketProductRepository marketProductRepository
    ) {
        this.variantVersionRepository = variantVersionRepository;
        this.lifecycleHistoryRepository = lifecycleHistoryRepository;
        this.productRepository = productRepository;
        this.marketLicenseRepository = marketLicenseRepository;
        this.marketProductRepository = marketProductRepository;
    }

    @Override
    public List<VariantVersionStatusCountDTO> getVariantVersionStatusCount() {
        return variantVersionRepository.countByStatus();
    }

    @Override
    public List<VariantVersionLifecycleHistoryResponseDTO>
    getVariantLifecycleHistory(Long variantId) {

        return lifecycleHistoryRepository
                .findByVariantIdWithRelations(variantId)
                .stream()
                .map(VariantVersionLifecycleHistoryResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public List<ProductCountByTherapeuticAreaDTO> getActiveProductCountByTherapeuticArea() {
        return productRepository.countActiveProductsByTherapeuticArea();
    }

    @Override
    public List<MarketLicenseStatusCountDTO> getMarketLicenseStatusCount() {
        return marketLicenseRepository.countByStatus();
    }

    @Override
    public List<MarketProductCountByRegionDTO> getActiveMarketProductCountByRegion() {
        return marketProductRepository.countActiveMarketProductsByRegion(EntityStatus.ACTIVE);
    }

}