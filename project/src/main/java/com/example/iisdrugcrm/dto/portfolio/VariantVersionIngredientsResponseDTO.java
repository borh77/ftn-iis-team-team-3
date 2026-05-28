package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionIngredients;

import java.math.BigDecimal;

public class VariantVersionIngredientsResponseDTO {

    private Long id;

    private Long variantVersionId;
    private String versionLabel;

    private Long variantId;
    private String productName;
    private String variantForm;
    private String variantDosage;

    private Long ingredientId;
    private String ingredientName;
    private String ingredientCas;
    private String ingredientChemicalFormula;

    private BigDecimal amount;
    private String unit;

    public static VariantVersionIngredientsResponseDTO fromEntity(VariantVersionIngredients item) {
        VariantVersionIngredientsResponseDTO dto = new VariantVersionIngredientsResponseDTO();

        dto.id = item.getId();

        dto.variantVersionId = item.getVariantVersion().getId();
        dto.versionLabel = item.getVariantVersion().getVersionLabel();

        dto.variantId = item.getVariantVersion().getVariant().getId();
        dto.productName = item.getVariantVersion().getVariant().getProduct().getName();
        dto.variantForm = item.getVariantVersion().getVariant().getForm();
        dto.variantDosage = item.getVariantVersion().getVariant().getDosage();

        dto.ingredientId = item.getIngredient().getId();
        dto.ingredientName = item.getIngredient().getName();
        dto.ingredientCas = item.getIngredient().getCas();
        dto.ingredientChemicalFormula = item.getIngredient().getChemicalFormula();

        dto.amount = item.getAmount();
        dto.unit = item.getUnit();

        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getVariantVersionId() {
        return variantVersionId;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public Long getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantForm() {
        return variantForm;
    }

    public String getVariantDosage() {
        return variantDosage;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getIngredientCas() {
        return ingredientCas;
    }

    public String getIngredientChemicalFormula() {
        return ingredientChemicalFormula;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }
}