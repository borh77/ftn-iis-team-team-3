package com.example.iisdrugcrm.dto.portfolio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VariantRequestDTO {

    @NotNull
    private Long productId;

    @NotBlank
    @Size(max = 100)
    private String form;

    @NotBlank
    @Size(max = 100)
    private String dosage;

    private Long replacementVariantId;

    public Long getProductId() {
        return productId;
    }

    public String getForm() {
        return form;
    }

    public String getDosage() {
        return dosage;
    }

    public Long getReplacementVariantId() {
        return replacementVariantId;
    }
}
