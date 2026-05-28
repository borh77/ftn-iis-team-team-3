package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

@Entity
@Table(
        name = "variant_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_variant_versions_variant_label",
                        columnNames = {"variant_id", "version_label"}
                )
        }
)
public class VariantVersion extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @Column(name = "version_label", nullable = false, length = 50)
    private String versionLabel;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VariantVersionStatus status =
            VariantVersionStatus.DEVELOPMENT;

    protected VariantVersion() {
    }

    public VariantVersion(
            Variant variant,
            String versionLabel,
            String description
    ) {
        this.variant = variant;
        this.versionLabel = versionLabel;
        this.description = description;
        this.status = VariantVersionStatus.DEVELOPMENT;
    }

    public void update(String description) {
        if (status != VariantVersionStatus.DEVELOPMENT) {
            throw new IllegalStateException(
                    "Only DEVELOPMENT versions can be updated"
            );
        }

        this.description = description;
    }

    public void changeStatus(VariantVersionStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Variant getVariant() {
        return variant;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getDescription() {
        return description;
    }

    public VariantVersionStatus getStatus() {
        return status;
    }
}