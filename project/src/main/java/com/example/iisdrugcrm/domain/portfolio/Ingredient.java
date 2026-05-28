package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

@Entity
@Table(
        name = "ingredients",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_ingredients_cas",
                        columnNames = "cas"
                )
        }
)
public class Ingredient extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "chemical_formula", length = 100)
    private String chemicalFormula;

    @Column(nullable = false, unique = true, length = 50)
    private String cas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IngredientType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status = EntityStatus.ACTIVE;

    protected Ingredient() {
    }

    public Ingredient(String name, String chemicalFormula, String cas, IngredientType type) {
        this.name = name;
        this.chemicalFormula = chemicalFormula;
        this.cas = cas;
        this.type = type;
        this.status = EntityStatus.ACTIVE;
    }

    public void update(String name, String chemicalFormula, String cas, IngredientType type) {
        this.name = name;
        this.chemicalFormula = chemicalFormula;
        this.cas = cas;
        this.type = type;
    }

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
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