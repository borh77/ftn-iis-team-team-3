package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistCreationStep;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class PricelistResponseDTO {

    private Long id;
    private Long regionId;
    private String regionName;
    private String customerSegment;
    private String currency;
    private PricelistStatus status;
    private Long createdBy;
    private Integer versionNumber;
    private Long parentPricelistId;
    private Long rootPricelistId;
    private PricelistCreationStep creationStep;
    private boolean creationCompleted;
    private OffsetDateTime lastEditedAt;
    private Long teamId;
    private String teamName;
    private boolean canCreateNewVersion;
    private boolean owner;
    private boolean canCollaborate;
    private boolean canEditDraft;
    private boolean canSubmitForReview;
    private boolean canManageOffers;
    private boolean canActivate;
    private boolean canReject;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private List<PricelistItemResponseDTO> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
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

    public PricelistCreationStep getCreationStep() {
        return creationStep;
    }

    public void setCreationStep(PricelistCreationStep creationStep) {
        this.creationStep = creationStep;
    }

    public boolean isCreationCompleted() {
        return creationCompleted;
    }

    public void setCreationCompleted(boolean creationCompleted) {
        this.creationCompleted = creationCompleted;
    }

    public OffsetDateTime getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(OffsetDateTime lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public boolean isCanCreateNewVersion() {
        return canCreateNewVersion;
    }

    public void setCanCreateNewVersion(boolean canCreateNewVersion) {
        this.canCreateNewVersion = canCreateNewVersion;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isCanCollaborate() {
        return canCollaborate;
    }

    public void setCanCollaborate(boolean canCollaborate) {
        this.canCollaborate = canCollaborate;
    }

    public boolean isCanManageOffers() {
        return canManageOffers;
    }

    public void setCanManageOffers(boolean canManageOffers) {
        this.canManageOffers = canManageOffers;
    }

    public boolean isCanActivate() {
        return canActivate;
    }

    public void setCanActivate(boolean canActivate) {
        this.canActivate = canActivate;
    }

    public boolean isCanEditDraft() {
        return canEditDraft;
    }

    public void setCanEditDraft(boolean canEditDraft) {
        this.canEditDraft = canEditDraft;
    }

    public boolean isCanSubmitForReview() {
        return canSubmitForReview;
    }

    public void setCanSubmitForReview(boolean canSubmitForReview) {
        this.canSubmitForReview = canSubmitForReview;
    }

    public boolean isCanReject() {
        return canReject;
    }

    public void setCanReject(boolean canReject) {
        this.canReject = canReject;
    }

    public void setPeriodEnd(OffsetDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public List<PricelistItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<PricelistItemResponseDTO> items) {
        this.items = items;
    }

    public static PricelistResponseDTO fromEntity(Pricelist pricelist) {
        return fromEntity(pricelist, Map.of());
    }

    public static PricelistResponseDTO fromEntity(Pricelist pricelist, Map<Long, CatalogVariantDTO> activeVariantsById) {
        PricelistResponseDTO dto = new PricelistResponseDTO();
        dto.setId(pricelist.getId());
        dto.setRegionId(pricelist.getRegion().getId());
        dto.setRegionName(pricelist.getRegion().getName());
        dto.setCustomerSegment(pricelist.getCustomerSegment());
        dto.setCurrency(pricelist.getCurrency());
        dto.setStatus(pricelist.getStatus());
        dto.setCreatedBy(pricelist.getCreatedBy());
        dto.setVersionNumber(pricelist.getVersionNumber());
        dto.setParentPricelistId(pricelist.getParentPricelistId());
        dto.setRootPricelistId(pricelist.getRootPricelistId());
        dto.setCreationStep(pricelist.getCreationStep());
        dto.setCreationCompleted(pricelist.isCreationCompleted());
        dto.setLastEditedAt(pricelist.getLastEditedAt());
        if (pricelist.getTeam() != null) {
            dto.setTeamId(pricelist.getTeam().getId());
            dto.setTeamName(pricelist.getTeam().getName());
        }
        dto.setCanCreateNewVersion(pricelist.getStatus() == PricelistStatus.ACTIVE);
        dto.setPeriodStart(pricelist.getPeriodStart());
        dto.setPeriodEnd(pricelist.getPeriodEnd());
        dto.setItems(pricelist.getItems().stream().map(item -> PricelistItemResponseDTO.fromEntity(item, activeVariantsById)).toList());
        return dto;
    }

    public static PricelistResponseDTO fromEntity(Pricelist pricelist, Long currentUserId, boolean canCollaborate, Map<Long, CatalogVariantDTO> activeVariantsById) {
        PricelistResponseDTO dto = fromEntity(pricelist, activeVariantsById);
        boolean owner = currentUserId != null
                && pricelist.getCreatedBy() != null
                && pricelist.getCreatedBy().equals(currentUserId);
        dto.setOwner(owner);
        dto.setCanCollaborate(canCollaborate);
        dto.setCanEditDraft(canCollaborate && pricelist.getStatus() == PricelistStatus.DRAFT);
        dto.setCanSubmitForReview(owner && isReadyForReview(pricelist, activeVariantsById));
        dto.setCanManageOffers(canCollaborate && pricelist.getStatus() == PricelistStatus.ACTIVE);
        dto.setCanCreateNewVersion(canCollaborate && pricelist.getStatus() == PricelistStatus.ACTIVE);
        return dto;
    }

    private static boolean isReadyForReview(Pricelist pricelist, Map<Long, CatalogVariantDTO> activeVariantsById) {
        if (pricelist.getStatus() != PricelistStatus.DRAFT) {
            return false;
        }
        if (pricelist.getCreationStep() != PricelistCreationStep.REVIEW
                && pricelist.getCreationStep() != PricelistCreationStep.COMPLETED) {
            return false;
        }
        if (pricelist.getRegion() == null) {
            return false;
        }
        if (pricelist.getCustomerSegment() == null
                || pricelist.getCustomerSegment().isBlank()
                || "UNDEFINED".equals(pricelist.getCustomerSegment())) {
            return false;
        }
        if (pricelist.getCurrency() == null || pricelist.getCurrency().isBlank()) {
            return false;
        }
        if (pricelist.getPeriodStart() == null
                || pricelist.getPeriodEnd() == null
                || !pricelist.getPeriodStart().isBefore(pricelist.getPeriodEnd())
                || isStartDateInPast(pricelist.getPeriodStart())) {
            return false;
        }
        if (pricelist.getItems() == null || pricelist.getItems().isEmpty()) {
            return false;
        }
        for (PricelistItem item : pricelist.getItems()) {
            if (item.getVariantId() == null || activeVariantsById == null || !activeVariantsById.containsKey(item.getVariantId())) {
                return false;
            }
            if (item.getThresholds() == null || item.getThresholds().isEmpty()) {
                return false;
            }
        }
        try {
            pricelist.validateThresholds();
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static boolean isStartDateInPast(OffsetDateTime periodStart) {
        ZoneId businessZone = ZoneId.systemDefault();
        LocalDate startDate = periodStart.atZoneSameInstant(businessZone).toLocalDate();
        return startDate.isBefore(LocalDate.now(businessZone));
    }

    public static class PricelistItemResponseDTO {
        private Long id;
        private Long variantId;
        private String variantName;
        private boolean activeVariant;
        private boolean replacementRequired;
        private boolean catalogAvailable;
        private List<QuantityThresholdResponseDTO> thresholds;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public boolean isActiveVariant() {
            return activeVariant;
        }

        public void setActiveVariant(boolean activeVariant) {
            this.activeVariant = activeVariant;
        }

        public boolean isReplacementRequired() {
            return replacementRequired;
        }

        public void setReplacementRequired(boolean replacementRequired) {
            this.replacementRequired = replacementRequired;
        }

        public boolean isCatalogAvailable() {
            return catalogAvailable;
        }

        public void setCatalogAvailable(boolean catalogAvailable) {
            this.catalogAvailable = catalogAvailable;
        }

        public List<QuantityThresholdResponseDTO> getThresholds() {
            return thresholds;
        }

        public void setThresholds(List<QuantityThresholdResponseDTO> thresholds) {
            this.thresholds = thresholds;
        }

        public static PricelistItemResponseDTO fromEntity(PricelistItem item) {
            return fromEntity(item, Map.of());
        }

        public static PricelistItemResponseDTO fromEntity(PricelistItem item, Map<Long, CatalogVariantDTO> activeVariantsById) {
            PricelistItemResponseDTO dto = new PricelistItemResponseDTO();
            dto.setId(item.getId());
            dto.setVariantId(item.getVariantId());
            dto.setVariantName(item.getVariantName());
            boolean active = activeVariantsById.containsKey(item.getVariantId());
            dto.setActiveVariant(active);
            dto.setCatalogAvailable(active);
            dto.setReplacementRequired(!active);
            dto.setThresholds(item.getThresholds().stream().map(QuantityThresholdResponseDTO::fromEntity).toList());
            return dto;
        }
    }

    public static class QuantityThresholdResponseDTO {
        private int quantityFrom;
        private Integer quantityTo;
        private BigDecimal price;

        public int getQuantityFrom() {
            return quantityFrom;
        }

        public void setQuantityFrom(int quantityFrom) {
            this.quantityFrom = quantityFrom;
        }

        public Integer getQuantityTo() {
            return quantityTo;
        }

        public void setQuantityTo(Integer quantityTo) {
            this.quantityTo = quantityTo;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public static QuantityThresholdResponseDTO fromEntity(QuantityThreshold threshold) {
            QuantityThresholdResponseDTO dto = new QuantityThresholdResponseDTO();
            dto.setQuantityFrom(threshold.getQuantityFrom());
            dto.setQuantityTo(threshold.getQuantityTo());
            dto.setPrice(threshold.getPrice());
            return dto;
        }
    }
}
