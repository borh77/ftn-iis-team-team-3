package com.example.iisdrugcrm.dto.order;

import java.math.BigDecimal;

public class ReplacementSuggestionDTO {

    private Long oldVariantId;
    private String oldVariantName;
    private Long newVariantId;
    private String newVariantName;
    private Integer requestedQuantity;
    private BigDecimal currentUnitPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal finalUnitPrice;
    private BigDecimal lineTotal;
    private String message;

    public Long getOldVariantId() {
        return oldVariantId;
    }

    public void setOldVariantId(Long oldVariantId) {
        this.oldVariantId = oldVariantId;
    }

    public String getOldVariantName() {
        return oldVariantName;
    }

    public void setOldVariantName(String oldVariantName) {
        this.oldVariantName = oldVariantName;
    }

    public Long getNewVariantId() {
        return newVariantId;
    }

    public void setNewVariantId(Long newVariantId) {
        this.newVariantId = newVariantId;
    }

    public String getNewVariantName() {
        return newVariantName;
    }

    public void setNewVariantName(String newVariantName) {
        this.newVariantName = newVariantName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public BigDecimal getCurrentUnitPrice() {
        return currentUnitPrice;
    }

    public void setCurrentUnitPrice(BigDecimal currentUnitPrice) {
        this.currentUnitPrice = currentUnitPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getFinalUnitPrice() {
        return finalUnitPrice;
    }

    public void setFinalUnitPrice(BigDecimal finalUnitPrice) {
        this.finalUnitPrice = finalUnitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
