package com.example.iisdrugcrm.dto.procurement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ConfirmProcurementItemDTO {

    @NotNull(message = "Variant is required.")
    private Long variantId;

    @NotNull(message = "Requested quantity is required.")
    @Positive(message = "Requested quantity must be positive.")
    private Integer requestedQuantity;

    private Long originalVariantId;
    private String originalVariantName;
    private boolean replacementAccepted;

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public Long getOriginalVariantId() { return originalVariantId; }
    public void setOriginalVariantId(Long originalVariantId) { this.originalVariantId = originalVariantId; }
    public String getOriginalVariantName() { return originalVariantName; }
    public void setOriginalVariantName(String originalVariantName) { this.originalVariantName = originalVariantName; }
    public boolean isReplacementAccepted() { return replacementAccepted; }
    public void setReplacementAccepted(boolean replacementAccepted) { this.replacementAccepted = replacementAccepted; }
}
