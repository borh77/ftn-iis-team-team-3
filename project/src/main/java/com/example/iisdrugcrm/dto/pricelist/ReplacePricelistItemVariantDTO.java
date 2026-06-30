package com.example.iisdrugcrm.dto.pricelist;

public class ReplacePricelistItemVariantDTO {

    private Long pricelistItemId;
    private Long replacementVariantId;

    public Long getPricelistItemId() {
        return pricelistItemId;
    }

    public void setPricelistItemId(Long pricelistItemId) {
        this.pricelistItemId = pricelistItemId;
    }

    public Long getReplacementVariantId() {
        return replacementVariantId;
    }

    public void setReplacementVariantId(Long replacementVariantId) {
        this.replacementVariantId = replacementVariantId;
    }
}
