package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.IngredientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IngredientUpdateDTO {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String chemicalFormula;

    @NotBlank
    @Size(max = 50)
    private String cas;

    @NotNull
    private IngredientType type;

    public String getName() {
        return name;
    }

    public String getChemicalFormula() {
        return chemicalFormula;
    }

    public String getCas() {
        return cas;
    }

    public IngredientType getType() {
        return type;
    }
}