package com.example.iisdrugcrm.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class MarketLicenseRequestDTO {

    @NotNull
    private Long marketProductId;

    @NotNull
    private Long variantVersionId;

    @NotBlank
    @Size(max = 100)
    private String licenseNumber;

    private LocalDate issuedAt;

    private LocalDate validUntil;

    public Long getMarketProductId() {
        return marketProductId;
    }

    public void setMarketProductId(Long marketProductId) {
        this.marketProductId = marketProductId;
    }

    public Long getVariantVersionId() {
        return variantVersionId;
    }

    public void setVariantVersionId(Long variantVersionId) {
        this.variantVersionId = variantVersionId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDate issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }
}