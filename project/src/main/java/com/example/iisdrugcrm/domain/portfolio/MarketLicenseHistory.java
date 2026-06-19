package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "market_license_history")
public class MarketLicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "market_license_id", nullable = false)
    private MarketLicense marketLicense;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private MarketLicenseStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private MarketLicenseStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(length = 1000)
    private String note;

    protected MarketLicenseHistory() {
    }

    public MarketLicenseHistory(
            MarketLicense marketLicense,
            MarketLicenseStatus oldStatus,
            MarketLicenseStatus newStatus,
            Long changedBy,
            String note
    ) {
        this.marketLicense = marketLicense;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.note = note;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public MarketLicense getMarketLicense() {
        return marketLicense;
    }

    public MarketLicenseStatus getOldStatus() {
        return oldStatus;
    }

    public MarketLicenseStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getNote() {
        return note;
    }
}