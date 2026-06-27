package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import java.math.BigDecimal;

public class PromotionSuggestionDTO {

    private Long variantId;
    private Long brandId;
    private String targetName;
    private String customerSegment;
    private DiscountType suggestedDiscountType;
    private BigDecimal suggestedDiscountValue;
    private String reason;
    private String expectedEffect;
    private String source;

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public DiscountType getSuggestedDiscountType() {
        return suggestedDiscountType;
    }

    public void setSuggestedDiscountType(DiscountType suggestedDiscountType) {
        this.suggestedDiscountType = suggestedDiscountType;
    }

    public BigDecimal getSuggestedDiscountValue() {
        return suggestedDiscountValue;
    }

    public void setSuggestedDiscountValue(BigDecimal suggestedDiscountValue) {
        this.suggestedDiscountValue = suggestedDiscountValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExpectedEffect() {
        return expectedEffect;
    }

    public void setExpectedEffect(String expectedEffect) {
        this.expectedEffect = expectedEffect;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
