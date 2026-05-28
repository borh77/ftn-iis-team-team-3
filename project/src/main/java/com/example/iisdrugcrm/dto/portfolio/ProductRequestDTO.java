package com.example.iisdrugcrm.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductRequestDTO {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private Long subcategoryId;

    @NotNull
    private Long therapeuticAreaId;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public Long getTherapeuticAreaId() {
        return therapeuticAreaId;
    }
}