package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersion;
import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;

public class VariantVersionResponseDTO {

    private Long id;

    private Long variantId;
    private String productName;
    private String variantForm;
    private String variantDosage;

    private String versionLabel;
    private String description;
    private VariantVersionStatus status;

    public static VariantVersionResponseDTO fromEntity(VariantVersion version) {
        VariantVersionResponseDTO dto = new VariantVersionResponseDTO();

        dto.id = version.getId();

        dto.variantId = version.getVariant().getId();
        dto.productName = version.getVariant().getProduct().getName();
        dto.variantForm = version.getVariant().getForm();
        dto.variantDosage = version.getVariant().getDosage();

        dto.versionLabel = version.getVersionLabel();
        dto.description = version.getDescription();
        dto.status = version.getStatus();

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

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getDescription() {
        return description;
    }

    public VariantVersionStatus getStatus() {
        return status;
    }
}