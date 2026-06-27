package com.example.iisdrugcrm.dto.order;

public class InvalidOrderItemDTO {

    private Long variantId;
    private Integer requestedQuantity;
    private String errorCode;
    private String message;

    public InvalidOrderItemDTO() {
    }

    public InvalidOrderItemDTO(Long variantId, Integer requestedQuantity, String errorCode, String message) {
        this.variantId = variantId;
        this.requestedQuantity = requestedQuantity;
        this.errorCode = errorCode;
        this.message = message;
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
