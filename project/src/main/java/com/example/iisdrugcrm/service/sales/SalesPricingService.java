package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.dto.sales.pricing.SalesPriceResponseDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SalesPricingService {

    private final PricelistRepository pricelistRepository;

    public SalesPricingService(PricelistRepository pricelistRepository) {
        this.pricelistRepository = pricelistRepository;
    }

    @Transactional(readOnly = true)
    public SalesPriceResponseDTO getPrice(Long regionId, Long variantId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Pricelist pricelist = pricelistRepository
                .findActiveBuyerPricelists(regionId, "Retail", OffsetDateTime.now())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Active pricelist not found for selected region."));

        PricelistItem item = pricelist.getItems()
                .stream()
                .filter(pricelistItem -> pricelistItem.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected product is not present in active pricelist."));

        QuantityThreshold threshold = item.getThresholds()
                .stream()
                .filter(t ->
                        quantity >= t.getQuantityFrom()
                                && (t.getQuantityTo() == null || quantity <= t.getQuantityTo())
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Price threshold not found for selected quantity."));

        return new SalesPriceResponseDTO(
                regionId,
                variantId,
                quantity,
                threshold.getPrice(),
                pricelist.getCurrency(),
                pricelist.getId()
        );
    }
}