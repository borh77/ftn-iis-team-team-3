package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "market_licenses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_market_licenses_market_product_version",
                        columnNames = {"market_product_id", "variant_version_id"}
                ),
                @UniqueConstraint(
                        name = "uq_market_licenses_license_number",
                        columnNames = {"license_number"}
                )
        }
)
public class MarketLicense extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "market_product_id", nullable = false)
    private MarketProduct marketProduct;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_version_id", nullable = false)
    private VariantVersion variantVersion;

    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "issued_at")
    private LocalDate issuedAt;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MarketLicenseStatus status = MarketLicenseStatus.SUBMITTED;

    protected MarketLicense() {
    }

    public MarketLicense(
            MarketProduct marketProduct,
            VariantVersion variantVersion,
            String licenseNumber,
            LocalDate issuedAt,
            LocalDate validUntil
    ) {
        this.marketProduct = marketProduct;
        this.variantVersion = variantVersion;
        this.licenseNumber = licenseNumber;
        this.issuedAt = issuedAt;
        this.validUntil = validUntil;
        this.status = MarketLicenseStatus.SUBMITTED;
    }

    public void update(
            String licenseNumber,
            LocalDate issuedAt,
            LocalDate validUntil
    ) {
        this.licenseNumber = licenseNumber;
        this.issuedAt = issuedAt;
        this.validUntil = validUntil;
    }

    public void changeStatus(MarketLicenseStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public MarketProduct getMarketProduct() {
        return marketProduct;
    }

    public VariantVersion getVariantVersion() {
        return variantVersion;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public LocalDate getIssuedAt() {
        return issuedAt;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public MarketLicenseStatus getStatus() {
        return status;
    }
}