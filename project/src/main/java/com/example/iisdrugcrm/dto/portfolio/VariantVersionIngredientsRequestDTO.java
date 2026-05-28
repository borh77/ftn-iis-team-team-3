package com.example.iisdrugcrm.dto.portfolio;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VariantVersionIngredientsRequestDTO {

    @NotNull
    private Long variantVersionId;

    @NotNull
    private Long ingredientId;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal amount;

    @NotBlank
    @Size(max = 30)
    private String unit;

    public Long getVariantVersionId() {
        return variantVersionId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }
}