package com.example.iisdrugcrm.domain.procurement;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "procurement_order_items")
public class ProcurementOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurement_order_id", nullable = false)
    private ProcurementOrder order;

    @Column(name = "original_variant_id")
    private Long originalVariantId;

    @Column(name = "original_variant_name")
    private String originalVariantName;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 30)
    private DiscountType discountType;

    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "final_unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalUnitPrice;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "replacement_accepted", nullable = false)
    private boolean replacementAccepted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProcurementOrder getOrder() { return order; }
    public void setOrder(ProcurementOrder order) { this.order = order; }
    public Long getOriginalVariantId() { return originalVariantId; }
    public void setOriginalVariantId(Long originalVariantId) { this.originalVariantId = originalVariantId; }
    public String getOriginalVariantName() { return originalVariantName; }
    public void setOriginalVariantName(String originalVariantName) { this.originalVariantName = originalVariantName; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getFinalUnitPrice() { return finalUnitPrice; }
    public void setFinalUnitPrice(BigDecimal finalUnitPrice) { this.finalUnitPrice = finalUnitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public boolean isReplacementAccepted() { return replacementAccepted; }
    public void setReplacementAccepted(boolean replacementAccepted) { this.replacementAccepted = replacementAccepted; }
}
