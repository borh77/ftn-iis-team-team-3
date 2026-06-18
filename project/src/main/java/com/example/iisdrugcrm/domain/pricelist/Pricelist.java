package com.example.iisdrugcrm.domain.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.exception.InvalidPricelistStatusTransitionException;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pricelists")
public class Pricelist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "customer_segment", nullable = false, length = 120)
    private String customerSegment;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PricelistStatus status = PricelistStatus.DRAFT;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @OneToMany(mappedBy = "pricelist", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PricelistItem> items = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "parent_pricelist_id")
    private Long parentPricelistId;

    @Column(name = "root_pricelist_id")
    private Long rootPricelistId;

    public Pricelist() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }

    public void setCustomerSegment(String customerSegment) {
        this.customerSegment = customerSegment;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PricelistStatus getStatus() {
        return status;
    }

    public void setStatus(PricelistStatus status) {
        this.status = status;
    }

    public OffsetDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(OffsetDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public OffsetDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(OffsetDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public List<PricelistItem> getItems() {
        return items;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Long getParentPricelistId() {
        return parentPricelistId;
    }

    public void setParentPricelistId(Long parentPricelistId) {
        this.parentPricelistId = parentPricelistId;
    }

    public Long getRootPricelistId() {
        return rootPricelistId;
    }

    public void setRootPricelistId(Long rootPricelistId) {
        this.rootPricelistId = rootPricelistId;
    }

    public void setItems(List<PricelistItem> items) {
        this.items = items;
    }

    public void addItem(PricelistItem item) {
        item.setPricelist(this);
        this.items.add(item);
    }

    public void validateThresholds() {
        for (PricelistItem item : items) {
            item.validateThresholds();
        }
    }

    public void changeStatus(PricelistStatus targetStatus, String reason) {
        if (targetStatus == null) {
            throw invalidTransition("Target status is required.");
        }
        if (status == PricelistStatus.ARCHIVED) {
            throw invalidTransition("Archived pricelists cannot change status.");
        }
        if (status == targetStatus) {
            throw invalidTransition("Pricelist is already in " + targetStatus + " status.");
        }

        switch (status) {
            case DRAFT -> {
                if (targetStatus != PricelistStatus.IN_REVIEW) {
                    throw invalidTransition("Pricelist can only be submitted for review from DRAFT status.");
                }
            }
            case IN_REVIEW -> {
                if (targetStatus == PricelistStatus.DRAFT && (reason == null || reason.isBlank())) {
                    throw invalidTransition("Return to draft requires a reason.");
                }
                if (targetStatus != PricelistStatus.ACTIVE && targetStatus != PricelistStatus.DRAFT) {
                    throw invalidTransition("Pricelists in review can only be activated or returned to draft.");
                }
            }
            case ACTIVE -> {
                if (targetStatus != PricelistStatus.ARCHIVED) {
                    throw invalidTransition("Active pricelists can only be archived.");
                }
            }
            default -> throw invalidTransition("Invalid pricelist status transition.");
        }

        this.status = targetStatus;
    }

    private InvalidPricelistStatusTransitionException invalidTransition(String message) {
        return new InvalidPricelistStatusTransitionException(message);
    }
}
