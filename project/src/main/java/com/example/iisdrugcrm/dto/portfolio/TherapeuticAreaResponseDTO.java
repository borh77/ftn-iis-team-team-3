package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.TherapeuticArea;

public class TherapeuticAreaResponseDTO {

    private Long id;
    private String name;
    private String description;
    private EntityStatus status;

    public static TherapeuticAreaResponseDTO fromEntity(TherapeuticArea area) {
        TherapeuticAreaResponseDTO dto = new TherapeuticAreaResponseDTO();
        dto.id = area.getId();
        dto.name = area.getName();
        dto.description = area.getDescription();
        dto.status = area.getStatus();
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
