package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProcurementPricingService {

    private final SpecialOfferRepository specialOfferRepository;

    public ProcurementPricingService(SpecialOfferRepository specialOfferRepository) {
        this.specialOfferRepository = specialOfferRepository;
    }

    public Map<Long, SpecialOffer> activeOffersByVariantId(Long pricelistId, OffsetDateTime now) {
        return specialOfferRepository.findActiveOffersForPricelist(pricelistId, now)
                .stream()
                .collect(Collectors.toMap(SpecialOffer::getVariantId, Function.identity(), (first, ignored) -> first));
    }

    public QuantityThreshold findMatchingThreshold(PricelistItem item, Integer requestedQuantity) {
        return item.getThresholds().stream()
                .sorted(Comparator.comparing(QuantityThreshold::getQuantityFrom))
                .filter(threshold -> threshold.getQuantityFrom() <= requestedQuantity)
                .filter(threshold -> threshold.getQuantityTo() == null || requestedQuantity <= threshold.getQuantityTo())
                .findFirst()
                .orElse(null);
    }

    public PricedLine priceLine(PricelistItem pricelistItem, Integer requestedQuantity, SpecialOffer activeOffer) {
        QuantityThreshold threshold = findMatchingThreshold(pricelistItem, requestedQuantity);
        if (threshold == null) {
            return null;
        }
        BigDecimal unitPrice = scaleMoney(threshold.getPrice());
        BigDecimal finalUnitPrice = applyDiscount(unitPrice, activeOffer);
        BigDecimal lineTotal = scaleMoney(finalUnitPrice.multiply(BigDecimal.valueOf(requestedQuantity)));
        DiscountType discountType = activeOffer == null ? null : activeOffer.getDiscountType();
        BigDecimal discountValue = activeOffer == null ? null : scaleMoney(activeOffer.getDiscountValue());
        return new PricedLine(unitPrice, discountType, discountValue, finalUnitPrice, lineTotal);
    }

    public BigDecimal applyDiscount(BigDecimal unitPrice, SpecialOffer offer) {
        if (offer == null) {
            return unitPrice;
        }
        BigDecimal discounted;
        if (offer.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal discount = unitPrice.multiply(offer.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            discounted = unitPrice.subtract(discount);
        } else {
            discounted = unitPrice.subtract(offer.getDiscountValue());
        }
        return scaleMoney(discounted.max(BigDecimal.ZERO));
    }

    public BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public List<Long> variantIds(List<PricelistItem> items) {
        return items.stream()
                .map(PricelistItem::getVariantId)
                .distinct()
                .toList();
    }

    public record PricedLine(
            BigDecimal unitPrice,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal finalUnitPrice,
            BigDecimal lineTotal
    ) {
    }
}
