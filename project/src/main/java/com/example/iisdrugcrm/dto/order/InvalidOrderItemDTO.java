package com.example.iisdrugcrm.dto.order;

public class InvalidOrderItemDTO {

    private Long variantId;
    private String variantName;
    private String productName;
    private String form;
    private String dosage;
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

    public InvalidOrderItemDTO(
            Long variantId,
            String variantName,
            String productName,
            String form,
            String dosage,
            Integer requestedQuantity,
            String errorCode,
            String message
    ) {
        this.variantId = variantId;
        this.variantName = variantName;
        this.productName = productName;
        this.form = form;
        this.dosage = dosage;
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

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
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
