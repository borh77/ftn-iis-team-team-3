package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Product;

public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;

    private Long subcategoryId;
    private String subcategoryName;

    private Long therapeuticAreaId;
    private String therapeuticAreaName;

    private EntityStatus status;

    public static ProductResponseDTO fromEntity(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.description = product.getDescription();

        dto.subcategoryId = product.getSubcategory().getId();
        dto.subcategoryName = product.getSubcategory().getName();

        dto.therapeuticAreaId = product.getTherapeuticArea().getId();
        dto.therapeuticAreaName = product.getTherapeuticArea().getName();

        dto.status = product.getStatus();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public Long getTherapeuticAreaId() {
        return therapeuticAreaId;
    }

    public String getTherapeuticAreaName() {
        return therapeuticAreaName;
    }

    public EntityStatus getStatus() {
        return status;
    }
}