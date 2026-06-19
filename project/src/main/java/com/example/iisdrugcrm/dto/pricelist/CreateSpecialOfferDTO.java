package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CreateSpecialOfferDTO {
    @NotNull
    private Long pricelistId;
    @NotNull
    private Long variantId;
    @NotNull
    private DiscountType discountType;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountValue;
    @NotNull
    private OffsetDateTime startDate;
    @NotNull
    private OffsetDateTime endDate;

    public Long getPricelistId() { return pricelistId; }
    public void setPricelistId(Long pricelistId) { this.pricelistId = pricelistId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
}
