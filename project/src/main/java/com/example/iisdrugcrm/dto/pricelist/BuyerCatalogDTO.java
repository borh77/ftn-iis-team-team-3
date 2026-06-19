package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BuyerCatalogDTO {

    private Long pricelistId;
    private String regionName;
    private String customerSegment;
    private String currency;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private List<BuyerCatalogItemDTO> items = List.of();

    public static BuyerCatalogDTO empty() {
        return new BuyerCatalogDTO();
    }

    public static BuyerCatalogDTO fromEntity(Pricelist pricelist) {
        return fromEntity(pricelist, Map.of());
    }

    public static BuyerCatalogDTO fromEntity(Pricelist pricelist, Map<Long, SpecialOffer> activeOffersByVariantId) {
        BuyerCatalogDTO dto = new BuyerCatalogDTO();
        dto.setPricelistId(pricelist.getId());
        dto.setRegionName(pricelist.getRegion().getName());
        dto.setCustomerSegment(pricelist.getCustomerSegment());
        dto.setCurrency(pricelist.getCurrency());
        dto.setPeriodStart(pricelist.getPeriodStart());
        dto.setPeriodEnd(pricelist.getPeriodEnd());
        dto.setItems(pricelist.getItems().stream()
                .map(item -> BuyerCatalogItemDTO.fromEntity(item, pricelist.getCurrency(), activeOffersByVariantId.get(item.getVariantId())))
                .toList());
        return dto;
    }

    public Long getPricelistId() {
        return pricelistId;
    }

    public void setPricelistId(Long pricelistId) {
        this.pricelistId = pricelistId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OffsetDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(OffsetDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public OffsetDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(OffsetDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public List<BuyerCatalogItemDTO> getItems() {
        return items;
    }

    public void setItems(List<BuyerCatalogItemDTO> items) {
        this.items = items;
    }

    public static class BuyerCatalogItemDTO {
        private Long variantId;
        private String variantName;
        private BigDecimal basePrice;
        private BigDecimal discountedPrice;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private boolean hasActiveOffer;
        private String currency;
        private List<ThresholdDTO> thresholds;

        public static BuyerCatalogItemDTO fromEntity(PricelistItem item, String currency) {
            return fromEntity(item, currency, null);
        }

        public static BuyerCatalogItemDTO fromEntity(PricelistItem item, String currency, SpecialOffer activeOffer) {
            BuyerCatalogItemDTO dto = new BuyerCatalogItemDTO();
            dto.setVariantId(item.getVariantId());
            dto.setVariantName(item.getVariantName());
            dto.setCurrency(currency);
            List<QuantityThreshold> sortedThresholds = item.getThresholds().stream()
                    .sorted(Comparator.comparing(QuantityThreshold::getQuantityFrom))
                    .toList();
            dto.setBasePrice(sortedThresholds.isEmpty() ? null : sortedThresholds.get(0).getPrice());
            if (activeOffer != null && dto.getBasePrice() != null) {
                dto.setHasActiveOffer(true);
                dto.setDiscountType(activeOffer.getDiscountType());
                dto.setDiscountValue(activeOffer.getDiscountValue());
                dto.setDiscountedPrice(calculateDiscountedPrice(dto.getBasePrice(), activeOffer));
            }
            dto.setThresholds(sortedThresholds.stream().map(ThresholdDTO::fromEntity).toList());
            return dto;
        }

        private static BigDecimal calculateDiscountedPrice(BigDecimal basePrice, SpecialOffer offer) {
            BigDecimal discounted;
            if (offer.getDiscountType() == DiscountType.PERCENTAGE) {
                BigDecimal discount = basePrice.multiply(offer.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                discounted = basePrice.subtract(discount);
            } else {
                discounted = basePrice.subtract(offer.getDiscountValue());
            }
            return discounted.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }

        public Long getVariantId() {
            return variantId;
        }

        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public BigDecimal getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
        }

        public BigDecimal getDiscountedPrice() {
            return discountedPrice;
        }

        public void setDiscountedPrice(BigDecimal discountedPrice) {
            this.discountedPrice = discountedPrice;
        }

        public DiscountType getDiscountType() {
            return discountType;
        }

        public void setDiscountType(DiscountType discountType) {
            this.discountType = discountType;
        }

        public BigDecimal getDiscountValue() {
            return discountValue;
        }

        public void setDiscountValue(BigDecimal discountValue) {
            this.discountValue = discountValue;
        }

        public boolean isHasActiveOffer() {
            return hasActiveOffer;
        }

        public void setHasActiveOffer(boolean hasActiveOffer) {
            this.hasActiveOffer = hasActiveOffer;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public List<ThresholdDTO> getThresholds() {
            return thresholds;
        }

        public void setThresholds(List<ThresholdDTO> thresholds) {
            this.thresholds = thresholds;
        }
    }

    public static class ThresholdDTO {
        private Integer quantityFrom;
        private Integer quantityTo;
        private BigDecimal price;

        public static ThresholdDTO fromEntity(QuantityThreshold threshold) {
            ThresholdDTO dto = new ThresholdDTO();
            dto.setQuantityFrom(threshold.getQuantityFrom());
            dto.setQuantityTo(threshold.getQuantityTo());
            dto.setPrice(threshold.getPrice());
            return dto;
        }

        public Integer getQuantityFrom() {
            return quantityFrom;
        }

        public void setQuantityFrom(Integer quantityFrom) {
            this.quantityFrom = quantityFrom;
        }

        public Integer getQuantityTo() {
            return quantityTo;
        }

        public void setQuantityTo(Integer quantityTo) {
            this.quantityTo = quantityTo;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
