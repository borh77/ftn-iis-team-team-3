package com.example.iisdrugcrm.dto.sales.pricing;

import java.math.BigDecimal;

public class SalesPriceResponseDTO {

    private Long regionId;
    private Long variantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String currency;
    private Long pricelistId;

    public SalesPriceResponseDTO(
            Long regionId,
            Long variantId,
            Integer quantity,
            BigDecimal unitPrice,
            String currency,
            Long pricelistId
    ) {
        this.regionId = regionId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.pricelistId = pricelistId;
    }

    public Long getRegionId() { return regionId; }
    public Long getVariantId() { return variantId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getCurrency() { return currency; }
    public Long getPricelistId() { return pricelistId; }
}