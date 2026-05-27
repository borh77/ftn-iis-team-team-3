package com.example.iisdrugcrm.dto;

import com.example.iisdrugcrm.domain.Region;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegionDTO {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String code;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static RegionDTO fromEntity(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setCode(region.getCode());
        return dto;
    }
}