package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicense;

import java.time.LocalDate;

public class MarketLicenseResponseDTO {

    private Long id;

    private Long marketProductId;
    private String localName;
    private String regionName;
    private String regionCode;

    private Long variantVersionId;
    private String productName;
    private String variantForm;
    private String variantDosage;
    private String versionLabel;

    private String licenseNumber;
    private LocalDate issuedAt;
    private LocalDate validUntil;
    private String status;

    public static MarketLicenseResponseDTO fromEntity(MarketLicense license) {
        MarketLicenseResponseDTO dto = new MarketLicenseResponseDTO();

        dto.setId(license.getId());

        dto.setMarketProductId(license.getMarketProduct().getId());
        dto.setLocalName(license.getMarketProduct().getLocalName());
        dto.setRegionName(license.getMarketProduct().getRegion().getName());
        dto.setRegionCode(license.getMarketProduct().getRegion().getCode());

        dto.setVariantVersionId(license.getVariantVersion().getId());
        dto.setProductName(license.getVariantVersion().getVariant().getProduct().getName());
        dto.setVariantForm(license.getVariantVersion().getVariant().getForm());
        dto.setVariantDosage(license.getVariantVersion().getVariant().getDosage());
        dto.setVersionLabel(license.getVariantVersion().getVersionLabel());

        dto.setLicenseNumber(license.getLicenseNumber());
        dto.setIssuedAt(license.getIssuedAt());
        dto.setValidUntil(license.getValidUntil());
        dto.setStatus(license.getStatus().name());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getMarketProductId() {
        return marketProductId;
    }

    public String getLocalName() {
        return localName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public Long getVariantVersionId() {
        return variantVersionId;
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

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMarketProductId(Long marketProductId) {
        this.marketProductId = marketProductId;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void setVariantVersionId(Long variantVersionId) {
        this.variantVersionId = variantVersionId;
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

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}