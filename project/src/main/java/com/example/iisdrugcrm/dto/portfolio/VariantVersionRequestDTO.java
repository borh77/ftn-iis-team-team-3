package com.example.iisdrugcrm.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VariantVersionRequestDTO {

    @NotNull
    private Long variantId;

    @NotBlank
    @Size(max = 50)
    private String versionLabel;

    @Size(max = 1000)
    private String description;

    public Long getVariantId() {
        return variantId;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getDescription() {
        return description;
    }
}