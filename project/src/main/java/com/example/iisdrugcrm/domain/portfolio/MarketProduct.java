package com.example.iisdrugcrm.domain.portfolio;

import com.example.iisdrugcrm.domain.Region;
import jakarta.persistence.*;

@Entity
@Table(
        name = "market_products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_market_products_variant_region",
                        columnNames = {"variant_id", "region_id"}
                ),
                @UniqueConstraint(
                        name = "uq_market_products_barcode",
                        columnNames = {"barcode"}
                )
        }
)
public class MarketProduct extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "local_name", nullable = false, length = 255)
    private String localName;

    @Column(name = "packaging_description", length = 500)
    private String packagingDescription;

    @Column(length = 100)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EntityStatus status = EntityStatus.ACTIVE;

    protected MarketProduct() {
    }

    public MarketProduct(
            Variant variant,
            Region region,
            String localName,
            String packagingDescription,
            String barcode
    ) {
        this.variant = variant;
        this.region = region;
        this.localName = localName;
        this.packagingDescription = packagingDescription;
        this.barcode = barcode;
        this.status = EntityStatus.ACTIVE;
    }

    public void update(
            String localName,
            String packagingDescription,
            String barcode
    ) {
        this.localName = localName;
        this.packagingDescription = packagingDescription;
        this.barcode = barcode;
    }

    public void archive() {
        this.status = EntityStatus.ARCHIVED;
    }

    public Long getId() {
        return id;
    }

    public Variant getVariant() {
        return variant;
    }

    public Region getRegion() {
        return region;
    }

    public String getLocalName() {
        return localName;
    }

    public String getPackagingDescription() {
        return packagingDescription;
    }

    public String getBarcode() {
        return barcode;
    }

    public EntityStatus getStatus() {
        return status;
    }
}