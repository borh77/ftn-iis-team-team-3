package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
    private boolean canCreateNewVersion;
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

    public boolean isCanCreateNewVersion() {
        return canCreateNewVersion;
    }

    public void setCanCreateNewVersion(boolean canCreateNewVersion) {
        this.canCreateNewVersion = canCreateNewVersion;
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
        dto.setCanCreateNewVersion(pricelist.getStatus() == PricelistStatus.IN_REVIEW || pricelist.getStatus() == PricelistStatus.ACTIVE);
        dto.setPeriodStart(pricelist.getPeriodStart());
        dto.setPeriodEnd(pricelist.getPeriodEnd());
        dto.setItems(pricelist.getItems().stream().map(PricelistItemResponseDTO::fromEntity).toList());
        return dto;
    }

    public static class PricelistItemResponseDTO {
        private Long id;
        private Long variantId;
        private String variantName;
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

        public List<QuantityThresholdResponseDTO> getThresholds() {
            return thresholds;
        }

        public void setThresholds(List<QuantityThresholdResponseDTO> thresholds) {
            this.thresholds = thresholds;
        }

        public static PricelistItemResponseDTO fromEntity(PricelistItem item) {
            PricelistItemResponseDTO dto = new PricelistItemResponseDTO();
            dto.setId(item.getId());
            dto.setVariantId(item.getVariantId());
            dto.setVariantName(item.getVariantName());
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
