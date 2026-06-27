package com.example.iisdrugcrm.dto.order;

public class OrderDocumentItemDTO {

    private Long variantId;
    private Integer requestedQuantity;

    public OrderDocumentItemDTO() {
    }

    public OrderDocumentItemDTO(Long variantId, Integer requestedQuantity) {
        this.variantId = variantId;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }
}
