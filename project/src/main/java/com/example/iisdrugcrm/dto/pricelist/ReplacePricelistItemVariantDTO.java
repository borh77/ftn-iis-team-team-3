package com.example.iisdrugcrm.dto.pricelist;

import jakarta.validation.constraints.NotNull;

public class ReplacePricelistItemVariantDTO {

    @NotNull
    private Long replacementVariantId;

    public Long getReplacementVariantId() {
        return replacementVariantId;
    }

    public void setReplacementVariantId(Long replacementVariantId) {
        this.replacementVariantId = replacementVariantId;
    }
}
