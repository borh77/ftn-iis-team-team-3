package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.MarketProductRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketProductResponseDTO;

import java.util.List;

public interface MarketProductService {

    List<MarketProductResponseDTO> getMarketProducts(
            String search,
            Long variantId,
            Long regionId,
            boolean includeArchived
    );

    MarketProductResponseDTO create(MarketProductRequestDTO dto);

    MarketProductResponseDTO update(Long id, MarketProductRequestDTO dto);

    void archive(Long id);
}