package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

@Entity
@Table(
        name = "variants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_variants_product_form_dosage",
                        columnNames = {"product_id", "form", "dosage"}
                )
        }
)
public class Variant extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String form;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status = EntityStatus.ACTIVE;

    protected Variant() {
    }

    public Variant(Product product, String form, String dosage) {
        this.product = product;
        this.form = form;
        this.dosage = dosage;
        this.status = EntityStatus.ACTIVE;
    }

    public void update(Product product, String form, String dosage) {
        this.product = product;
        this.form = form;
        this.dosage = dosage;
    }

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getForm() {
        return form;
    }

    public String getDosage() {
        return dosage;
    }

    public EntityStatus getStatus() {
        return status;
    }
}