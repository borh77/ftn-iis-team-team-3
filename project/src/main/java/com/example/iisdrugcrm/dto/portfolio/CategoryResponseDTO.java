package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.Category;
import com.example.iisdrugcrm.domain.portfolio.EntityStatus;

public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;
    private EntityStatus status;

    public static CategoryResponseDTO fromEntity(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.id = category.getId();
        dto.name = category.getName();
        dto.description = category.getDescription();
        dto.status = category.getStatus();
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

    public EntityStatus getStatus() {
        return status;
    }
}
