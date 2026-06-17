package com.example.iisdrugcrm.domain.portfolio;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "variant_version_lifecycle_history")
public class VariantVersionLifecycleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_version_id", nullable = false)
    private VariantVersion variantVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private VariantVersionStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private VariantVersionStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(length = 1000)
    private String reason;

    @Column(name = "automatic_transition", nullable = false)
    private boolean automaticTransition;

    protected VariantVersionLifecycleHistory() {
    }

    public VariantVersionLifecycleHistory(
            VariantVersion variantVersion,
            VariantVersionStatus oldStatus,
            VariantVersionStatus newStatus,
            Long changedBy,
            String reason,
            boolean automaticTransition
    ) {
        this.variantVersion = variantVersion;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.reason = reason;
        this.automaticTransition = automaticTransition;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public VariantVersion getVariantVersion() {
        return variantVersion;
    }

    public VariantVersionStatus getOldStatus() {
        return oldStatus;
    }

    public VariantVersionStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public Long getChangedBy() {
        return changedBy;
    }

    public String getReason() {
        return reason;
    }

    public boolean isAutomaticTransition() {
        return automaticTransition;
    }
}