package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", nullable = false)
    private Subcategory subcategory;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "therapeutic_area_id", nullable = false)
    private TherapeuticArea therapeuticArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status = EntityStatus.ACTIVE;

    protected Product() {
    }

    public Product(
            String name,
            String description,
            Subcategory subcategory,
            TherapeuticArea therapeuticArea
    ) {
        this.name = name;
        this.description = description;
        this.subcategory = subcategory;
        this.therapeuticArea = therapeuticArea;
        this.status = EntityStatus.ACTIVE;
    }

    public void update(
            String name,
            String description,
            Subcategory subcategory,
            TherapeuticArea therapeuticArea
    ) {
        this.name = name;
        this.description = description;
        this.subcategory = subcategory;
        this.therapeuticArea = therapeuticArea;
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

    public String getDescription() {
        return description;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public TherapeuticArea getTherapeuticArea() {
        return therapeuticArea;
    }

    public EntityStatus getStatus() {
        return status;
    }
}