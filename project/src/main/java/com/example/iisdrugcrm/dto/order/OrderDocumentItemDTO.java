package com.example.iisdrugcrm.dto.order;

public class OrderDocumentItemDTO {

    private Long variantId;
    private String variantName;
    private String productName;
    private String form;
    private String dosage;
    private Integer requestedQuantity;

    public OrderDocumentItemDTO() {
    }

    public OrderDocumentItemDTO(Long variantId, Integer requestedQuantity) {
        this.variantId = variantId;
        this.requestedQuantity = requestedQuantity;
    }

    public OrderDocumentItemDTO(String variantName, Integer requestedQuantity) {
        this.variantName = variantName;
        this.requestedQuantity = requestedQuantity;
    }

    public OrderDocumentItemDTO(String productName, String form, String dosage, Integer requestedQuantity) {
        this.productName = productName;
        this.form = form;
        this.dosage = dosage;
        this.requestedQuantity = requestedQuantity;
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
}
