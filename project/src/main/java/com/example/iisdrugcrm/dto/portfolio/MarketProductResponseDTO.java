package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketProduct;

public class MarketProductResponseDTO {

    private Long id;

    private Long variantId;
    private String productName;
    private String variantForm;
    private String variantDosage;

    private Long regionId;
    private String regionName;
    private String regionCode;

    private String localName;
    private String packagingDescription;
    private String barcode;
    private String status;

    public static MarketProductResponseDTO fromEntity(MarketProduct marketProduct) {
        MarketProductResponseDTO dto = new MarketProductResponseDTO();

        dto.setId(marketProduct.getId());

        dto.setVariantId(marketProduct.getVariant().getId());
        dto.setProductName(marketProduct.getVariant().getProduct().getName());
        dto.setVariantForm(marketProduct.getVariant().getForm());
        dto.setVariantDosage(marketProduct.getVariant().getDosage());

        dto.setRegionId(marketProduct.getRegion().getId());
        dto.setRegionName(marketProduct.getRegion().getName());
        dto.setRegionCode(marketProduct.getRegion().getCode());

        dto.setLocalName(marketProduct.getLocalName());
        dto.setPackagingDescription(marketProduct.getPackagingDescription());
        dto.setBarcode(marketProduct.getBarcode());
        dto.setStatus(marketProduct.getStatus().name());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantForm() {
        return variantForm;
    }

    public String getVariantDosage() {
        return variantDosage;
    }

    public Long getRegionId() {
        return regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getLocalName() {
        return localName;
    }

    public String getPackagingDescription() {
        return packagingDescription;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setVariantForm(String variantForm) {
        this.variantForm = variantForm;
    }

    public void setVariantDosage(String variantDosage) {
        this.variantDosage = variantDosage;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void setPackagingDescription(String packagingDescription) {
        this.packagingDescription = packagingDescription;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}