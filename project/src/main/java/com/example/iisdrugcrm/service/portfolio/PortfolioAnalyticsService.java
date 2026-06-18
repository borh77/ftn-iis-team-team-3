package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusCountDTO;

import com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO;

import java.util.List;

public interface PortfolioAnalyticsService {

    List<VariantVersionStatusCountDTO> getVariantVersionStatusCount();

    List<VariantVersionLifecycleHistoryResponseDTO>
    getVariantLifecycleHistory(Long variantId);

    List<ProductCountByTherapeuticAreaDTO> getActiveProductCountByTherapeuticArea();
}