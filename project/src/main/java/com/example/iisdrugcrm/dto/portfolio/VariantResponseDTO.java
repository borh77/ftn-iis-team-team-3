package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Variant;

public class VariantResponseDTO {

    private Long id;

    private Long productId;
    private String productName;

    private String form;
    private String dosage;

    private EntityStatus status;

    public static VariantResponseDTO fromEntity(Variant variant) {
        VariantResponseDTO dto = new VariantResponseDTO();
        dto.id = variant.getId();

        dto.productId = variant.getProduct().getId();
        dto.productName = variant.getProduct().getName();

        dto.form = variant.getForm();
        dto.dosage = variant.getDosage();

        dto.status = variant.getStatus();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getForm() {
        return form;
    }

    public String getDosage() {
        return dosage;
    }

    public EntityStatus getStatus() {
        return status;
    }
}