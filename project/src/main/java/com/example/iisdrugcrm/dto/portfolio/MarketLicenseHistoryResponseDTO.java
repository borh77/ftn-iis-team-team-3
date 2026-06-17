package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseHistory;

import java.time.LocalDateTime;

public class MarketLicenseHistoryResponseDTO {

    private Long id;
    private Long marketLicenseId;

    private String licenseNumber;
    private String localName;
    private String regionName;
    private String productName;
    private String versionLabel;

    private String oldStatus;
    private String newStatus;

    private LocalDateTime changedAt;
    private Long changedBy;
    private String note;

    public static MarketLicenseHistoryResponseDTO fromEntity(MarketLicenseHistory history) {
        MarketLicenseHistoryResponseDTO dto = new MarketLicenseHistoryResponseDTO();

        dto.setId(history.getId());
        dto.setMarketLicenseId(history.getMarketLicense().getId());

        dto.setLicenseNumber(history.getMarketLicense().getLicenseNumber());
        dto.setLocalName(history.getMarketLicense().getMarketProduct().getLocalName());
        dto.setRegionName(history.getMarketLicense().getMarketProduct().getRegion().getName());
        dto.setProductName(history.getMarketLicense().getVariantVersion().getVariant().getProduct().getName());
        dto.setVersionLabel(history.getMarketLicense().getVariantVersion().getVersionLabel());

        dto.setOldStatus(history.getOldStatus() == null ? null : history.getOldStatus().name());
        dto.setNewStatus(history.getNewStatus().name());

        dto.setChangedAt(history.getChangedAt());
        dto.setChangedBy(history.getChangedBy());
        dto.setNote(history.getNote());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getMarketLicenseId() {
        return marketLicenseId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getLocalName() {
        return localName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getProductName() {
        return productName;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getNote() {
        return note;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMarketLicenseId(Long marketLicenseId) {
        this.marketLicenseId = marketLicenseId;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public void setNote(String note) {
        this.note = note;
    }
}