package com.example.iisdrugcrm.dto.procurement;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.procurement.ProcurementOrderItem;
import java.math.BigDecimal;

public class ProcurementOrderItemResponseDTO {

    private Long id;
    private Long originalVariantId;
    private String originalVariantName;
    private Long variantId;
    private String variantName;
    private Integer requestedQuantity;
    private BigDecimal unitPrice;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal finalUnitPrice;
    private BigDecimal lineTotal;
    private boolean replacementAccepted;

    public static ProcurementOrderItemResponseDTO fromEntity(ProcurementOrderItem item) {
        ProcurementOrderItemResponseDTO dto = new ProcurementOrderItemResponseDTO();
        dto.setId(item.getId());
        dto.setOriginalVariantId(item.getOriginalVariantId());
        dto.setOriginalVariantName(item.getOriginalVariantName());
        dto.setVariantId(item.getVariantId());
        dto.setVariantName(item.getVariantName());
        dto.setRequestedQuantity(item.getRequestedQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountType(item.getDiscountType());
        dto.setDiscountValue(item.getDiscountValue());
        dto.setFinalUnitPrice(item.getFinalUnitPrice());
        dto.setLineTotal(item.getLineTotal());
        dto.setReplacementAccepted(item.isReplacementAccepted());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOriginalVariantId() { return originalVariantId; }
    public void setOriginalVariantId(Long originalVariantId) { this.originalVariantId = originalVariantId; }
    public String getOriginalVariantName() { return originalVariantName; }
    public void setOriginalVariantName(String originalVariantName) { this.originalVariantName = originalVariantName; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getFinalUnitPrice() { return finalUnitPrice; }
    public void setFinalUnitPrice(BigDecimal finalUnitPrice) { this.finalUnitPrice = finalUnitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public boolean isReplacementAccepted() { return replacementAccepted; }
    public void setReplacementAccepted(boolean replacementAccepted) { this.replacementAccepted = replacementAccepted; }
}
