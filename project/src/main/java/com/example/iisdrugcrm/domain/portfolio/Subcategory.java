package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

@Entity
@Table(
        name = "subcategories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_subcategories_category_name",
                        columnNames = {"category_id", "name"}
                )
        }
)
public class Subcategory extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status = EntityStatus.ACTIVE;

    protected Subcategory() {
    }

    public Subcategory(Category category, String name, String description) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.status = EntityStatus.ACTIVE;
    }

    public void update(Category category, String name, String description) {
        this.category = category;
        this.name = name;
        this.description = description;
    }

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityStatus getStatus() {
        return status;
    }
}