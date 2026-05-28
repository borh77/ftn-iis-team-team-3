package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Ingredient;
import com.example.iisdrugcrm.domain.portfolio.IngredientType;

public class IngredientResponseDTO {

    private Long id;
    private String name;
    private String chemicalFormula;
    private String cas;
    private IngredientType type;
    private EntityStatus status;

    public static IngredientResponseDTO fromEntity(Ingredient ingredient) {
        IngredientResponseDTO dto = new IngredientResponseDTO();
        dto.id = ingredient.getId();
        dto.name = ingredient.getName();
        dto.chemicalFormula = ingredient.getChemicalFormula();
        dto.cas = ingredient.getCas();
        dto.type = ingredient.getType();
        dto.status = ingredient.getStatus();
        return dto;
    }

    public Long getId() {
        return id;
    }

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

    public EntityStatus getStatus() {
        return status;
    }
}