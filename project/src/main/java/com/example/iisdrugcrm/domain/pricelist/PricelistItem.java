package com.example.iisdrugcrm.domain.pricelist;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pricelist_items")
public class PricelistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricelist_id", nullable = false)
    private Pricelist pricelist;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false, length = 255)
    private String variantName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pricelist_item_thresholds", joinColumns = @JoinColumn(name = "pricelist_item_id"))
    @OrderBy("quantityFrom ASC")
    private List<QuantityThreshold> thresholds = new ArrayList<>();

    public PricelistItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pricelist getPricelist() {
        return pricelist;
    }

    public void setPricelist(Pricelist pricelist) {
        this.pricelist = pricelist;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public List<QuantityThreshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<QuantityThreshold> thresholds) {
        this.thresholds = thresholds;
    }
}