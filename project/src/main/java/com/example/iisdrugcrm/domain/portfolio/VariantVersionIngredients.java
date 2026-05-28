package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "variant_version_ingredients",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_vvi_version_ingredient",
                        columnNames = {"variant_version_id", "ingredient_id"}
                )
        }
)
public class VariantVersionIngredients extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_version_id", nullable = false)
    private VariantVersion variantVersion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 30)
    private String unit;

    protected VariantVersionIngredients() {
    }

    public VariantVersionIngredients(
            VariantVersion variantVersion,
            Ingredient ingredient,
            BigDecimal amount,
            String unit
    ) {
        this.variantVersion = variantVersion;
        this.ingredient = ingredient;
        this.amount = amount;
        this.unit = unit;
    }

    public void update(
        VariantVersion variantVersion,
        Ingredient ingredient,
        BigDecimal amount,
        String unit
    ) {
    this.variantVersion = variantVersion;
    this.ingredient = ingredient;
    this.amount = amount;
    this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public VariantVersion getVariantVersion() {
        return variantVersion;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }
}