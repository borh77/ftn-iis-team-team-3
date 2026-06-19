package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.MarketProduct;
import com.example.iisdrugcrm.domain.portfolio.Variant;
import com.example.iisdrugcrm.dto.portfolio.MarketProductRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketProductResponseDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.RegionRepository;
import com.example.iisdrugcrm.repository.portfolio.MarketProductRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarketProductServiceImpl implements MarketProductService {

    private final MarketProductRepository marketProductRepository;
    private final VariantRepository variantRepository;
    private final RegionRepository regionRepository;

    public MarketProductServiceImpl(
            MarketProductRepository marketProductRepository,
            VariantRepository variantRepository,
            RegionRepository regionRepository
    ) {
        this.marketProductRepository = marketProductRepository;
        this.variantRepository = variantRepository;
        this.regionRepository = regionRepository;
    }

    @Override
    public List<MarketProductResponseDTO> getMarketProducts(
            String search,
            Long variantId,
            Long regionId,
            boolean includeArchived
    ) {
        List<MarketProduct> marketProducts;

        if (search != null && !search.isBlank()) {
            marketProducts = marketProductRepository.searchByTextWithRelations(search.trim());
        } else if (variantId != null) {
            marketProducts = marketProductRepository.findByVariantIdWithRelations(
                    variantId,
                    EntityStatus.ACTIVE
            );
        } else if (regionId != null) {
            marketProducts = marketProductRepository.findByRegionIdWithRelations(
                    regionId,
                    EntityStatus.ACTIVE
            );
        } else if (includeArchived) {
            marketProducts = marketProductRepository.findAllWithRelations();
        } else {
            marketProducts = marketProductRepository.findByStatusWithRelations(EntityStatus.ACTIVE);
        }

        return marketProducts.stream()
                .map(MarketProductResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public MarketProductResponseDTO create(MarketProductRequestDTO dto) {
        if (marketProductRepository.existsByVariantIdAndRegionId(
                dto.getVariantId(),
                dto.getRegionId()
        )) {
            throw new PortfolioDuplicateResourceException(
                    "Market product already exists for this variant and region"
            );
        }

        validateUniqueBarcode(dto.getBarcode(), null);

        Variant variant = getVariant(dto.getVariantId());
        Region region = getRegion(dto.getRegionId());

        if (variant.getStatus() != EntityStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Market product can only be created for ACTIVE variants"
            );
        }

        MarketProduct marketProduct = new MarketProduct(
                variant,
                region,
                normalize(dto.getLocalName()),
                normalize(dto.getPackagingDescription()),
                normalize(dto.getBarcode())
        );

        return MarketProductResponseDTO.fromEntity(
                marketProductRepository.save(marketProduct)
        );
    }

    @Override
    @Transactional
    public MarketProductResponseDTO update(Long id, MarketProductRequestDTO dto) {
        MarketProduct marketProduct = getMarketProduct(id);

        if (!marketProduct.getVariant().getId().equals(dto.getVariantId())) {
            throw new IllegalStateException(
                    "Variant cannot be changed for an existing market product"
            );
        }

        if (!marketProduct.getRegion().getId().equals(dto.getRegionId())) {
            throw new IllegalStateException(
                    "Region cannot be changed for an existing market product"
            );
        }

        validateUniqueBarcode(dto.getBarcode(), id);

        marketProduct.update(
                normalize(dto.getLocalName()),
                normalize(dto.getPackagingDescription()),
                normalize(dto.getBarcode())
        );

        return MarketProductResponseDTO.fromEntity(
                marketProductRepository.save(marketProduct)
        );
    }

    @Override
    @Transactional
    public void archive(Long id) {
        MarketProduct marketProduct = getMarketProduct(id);
        marketProduct.archive();
        marketProductRepository.save(marketProduct);
    }

    private MarketProduct getMarketProduct(Long id) {
        return marketProductRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Market product not found"));
    }

    private Variant getVariant(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Variant not found"));
    }

    private Region getRegion(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Region not found"));
    }

    private void validateUniqueBarcode(String barcode, Long currentId) {
        String normalizedBarcode = normalize(barcode);

        if (normalizedBarcode == null || normalizedBarcode.isBlank()) {
            return;
        }

        boolean duplicate = currentId == null
                ? marketProductRepository.existsByBarcodeIgnoreCase(normalizedBarcode)
                : marketProductRepository.existsByBarcodeIgnoreCaseAndIdNot(normalizedBarcode, currentId);

        if (duplicate) {
            throw new PortfolioDuplicateResourceException("Barcode already exists");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}