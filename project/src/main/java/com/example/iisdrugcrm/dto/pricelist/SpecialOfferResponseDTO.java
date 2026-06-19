package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class SpecialOfferResponseDTO {
    private Long id;
    private Long pricelistId;
    private Long variantId;
    private String variantName;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private SpecialOfferStatus status;

    public static SpecialOfferResponseDTO fromEntity(SpecialOffer offer) {
        SpecialOfferResponseDTO dto = new SpecialOfferResponseDTO();
        dto.setId(offer.getId());
        dto.setPricelistId(offer.getPricelist().getId());
        dto.setVariantId(offer.getVariantId());
        dto.setVariantName(offer.getVariantName());
        dto.setDiscountType(offer.getDiscountType());
        dto.setDiscountValue(offer.getDiscountValue());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setStatus(offer.getStatus());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPricelistId() { return pricelistId; }
    public void setPricelistId(Long pricelistId) { this.pricelistId = pricelistId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
    public SpecialOfferStatus getStatus() { return status; }
    public void setStatus(SpecialOfferStatus status) { this.status = status; }
}
