package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Subcategory;

public class SubcategoryResponseDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private EntityStatus status;

    public static SubcategoryResponseDTO fromEntity(Subcategory subcategory) {
        SubcategoryResponseDTO dto = new SubcategoryResponseDTO();
        dto.id = subcategory.getId();
        dto.categoryId = subcategory.getCategory().getId();
        dto.categoryName = subcategory.getCategory().getName();
        dto.name = subcategory.getName();
        dto.description = subcategory.getDescription();
        dto.status = subcategory.getStatus();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityStatus getStatus() {
        return status;
    }
}
