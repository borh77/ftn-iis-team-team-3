package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionLifecycleHistory;

import java.time.LocalDateTime;

public class VariantVersionLifecycleHistoryResponseDTO {

    private Long id;
    private Long variantVersionId;

    private String productName;
    private String variantForm;
    private String variantDosage;
    private String versionLabel;

    private String oldStatus;
    private String newStatus;

    private LocalDateTime changedAt;
    private Long changedBy;
    private String reason;
    private boolean automaticTransition;

    public static VariantVersionLifecycleHistoryResponseDTO fromEntity(
            VariantVersionLifecycleHistory history
    ) {
        VariantVersionLifecycleHistoryResponseDTO dto =
                new VariantVersionLifecycleHistoryResponseDTO();

        dto.setId(history.getId());
        dto.setVariantVersionId(history.getVariantVersion().getId());

        dto.setProductName(
                history.getVariantVersion().getVariant().getProduct().getName()
        );
        dto.setVariantForm(
                history.getVariantVersion().getVariant().getForm()
        );
        dto.setVariantDosage(
                history.getVariantVersion().getVariant().getDosage()
        );
        dto.setVersionLabel(
                history.getVariantVersion().getVersionLabel()
        );

        dto.setOldStatus(
                history.getOldStatus() == null ? null : history.getOldStatus().name()
        );
        dto.setNewStatus(history.getNewStatus().name());

        dto.setChangedAt(history.getChangedAt());
        dto.setChangedBy(history.getChangedBy());
        dto.setReason(history.getReason());
        dto.setAutomaticTransition(history.isAutomaticTransition());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getVariantVersionId() {
        return variantVersionId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantForm() {
        return variantForm;
    }

    public String getVariantDosage() {
        return variantDosage;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setVariantVersionId(Long variantVersionId) {
        this.variantVersionId = variantVersionId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setVariantForm(String variantForm) {
        this.variantForm = variantForm;
    }

    public void setVariantDosage(String variantDosage) {
        this.variantDosage = variantDosage;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public void setChangedBy(Long changedBy) {
        this.changedBy = changedBy;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setAutomaticTransition(boolean automaticTransition) {
        this.automaticTransition = automaticTransition;
    }
}