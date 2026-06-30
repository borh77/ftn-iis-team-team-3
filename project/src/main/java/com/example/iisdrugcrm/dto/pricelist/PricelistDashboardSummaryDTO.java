package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PricelistDashboardSummaryDTO {

    private long totalPricelists;
    private long draftCount;
    private long inReviewCount;
    private long activeCount;
    private long archivedCount;
    private long waitingForReviewCount;
    private long incompleteDraftCount;
    private long stuckDraftCount;
    private long stuckInReviewCount;
    private long activeOffersCount;
    private long activatedPricelistsCount;
    private BigDecimal averageProcessingTimeHours = BigDecimal.ZERO;
    private BigDecimal averageReviewTimeHours = BigDecimal.ZERO;
    private Map<PricelistStatus, Long> statusCounts = new EnumMap<>(PricelistStatus.class);
    private List<BreakdownItemDTO> pricelistsByRegion = new ArrayList<>();
    private List<BreakdownItemDTO> pricelistsBySegment = new ArrayList<>();
    private List<BreakdownItemDTO> pricelistsByTeam = new ArrayList<>();
    private List<BreakdownItemDTO> activityCountByActionType = new ArrayList<>();
    private List<RecentPricelistDTO> recentPricelists = new ArrayList<>();
    private List<PricelistActivityLogResponseDTO> recentActivity = new ArrayList<>();
    private DashboardFiltersDTO appliedFilters = new DashboardFiltersDTO();

    public long getTotalPricelists() { return totalPricelists; }
    public void setTotalPricelists(long totalPricelists) { this.totalPricelists = totalPricelists; }
    public long getDraftCount() { return draftCount; }
    public void setDraftCount(long draftCount) { this.draftCount = draftCount; }
    public long getInReviewCount() { return inReviewCount; }
    public void setInReviewCount(long inReviewCount) { this.inReviewCount = inReviewCount; }
    public long getActiveCount() { return activeCount; }
    public void setActiveCount(long activeCount) { this.activeCount = activeCount; }
    public long getArchivedCount() { return archivedCount; }
    public void setArchivedCount(long archivedCount) { this.archivedCount = archivedCount; }
    public long getWaitingForReviewCount() { return waitingForReviewCount; }
    public void setWaitingForReviewCount(long waitingForReviewCount) { this.waitingForReviewCount = waitingForReviewCount; }
    public long getIncompleteDraftCount() { return incompleteDraftCount; }
    public void setIncompleteDraftCount(long incompleteDraftCount) { this.incompleteDraftCount = incompleteDraftCount; }
    public long getStuckDraftCount() { return stuckDraftCount; }
    public void setStuckDraftCount(long stuckDraftCount) { this.stuckDraftCount = stuckDraftCount; }
    public long getStuckInReviewCount() { return stuckInReviewCount; }
    public void setStuckInReviewCount(long stuckInReviewCount) { this.stuckInReviewCount = stuckInReviewCount; }
    public long getActiveOffersCount() { return activeOffersCount; }
    public void setActiveOffersCount(long activeOffersCount) { this.activeOffersCount = activeOffersCount; }
    public long getActivatedPricelistsCount() { return activatedPricelistsCount; }
    public void setActivatedPricelistsCount(long activatedPricelistsCount) { this.activatedPricelistsCount = activatedPricelistsCount; }
    public BigDecimal getAverageProcessingTimeHours() { return averageProcessingTimeHours; }
    public void setAverageProcessingTimeHours(BigDecimal averageProcessingTimeHours) { this.averageProcessingTimeHours = averageProcessingTimeHours; }
    public BigDecimal getAverageReviewTimeHours() { return averageReviewTimeHours; }
    public void setAverageReviewTimeHours(BigDecimal averageReviewTimeHours) { this.averageReviewTimeHours = averageReviewTimeHours; }
    public Map<PricelistStatus, Long> getStatusCounts() { return statusCounts; }
    public void setStatusCounts(Map<PricelistStatus, Long> statusCounts) { this.statusCounts = statusCounts; }
    public List<BreakdownItemDTO> getPricelistsByRegion() { return pricelistsByRegion; }
    public void setPricelistsByRegion(List<BreakdownItemDTO> pricelistsByRegion) { this.pricelistsByRegion = pricelistsByRegion; }
    public List<BreakdownItemDTO> getPricelistsBySegment() { return pricelistsBySegment; }
    public void setPricelistsBySegment(List<BreakdownItemDTO> pricelistsBySegment) { this.pricelistsBySegment = pricelistsBySegment; }
    public List<BreakdownItemDTO> getPricelistsByTeam() { return pricelistsByTeam; }
    public void setPricelistsByTeam(List<BreakdownItemDTO> pricelistsByTeam) { this.pricelistsByTeam = pricelistsByTeam; }
    public List<BreakdownItemDTO> getActivityCountByActionType() { return activityCountByActionType; }
    public void setActivityCountByActionType(List<BreakdownItemDTO> activityCountByActionType) { this.activityCountByActionType = activityCountByActionType; }
    public List<RecentPricelistDTO> getRecentPricelists() { return recentPricelists; }
    public void setRecentPricelists(List<RecentPricelistDTO> recentPricelists) { this.recentPricelists = recentPricelists; }
    public List<PricelistActivityLogResponseDTO> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<PricelistActivityLogResponseDTO> recentActivity) { this.recentActivity = recentActivity; }
    public DashboardFiltersDTO getAppliedFilters() { return appliedFilters; }
    public void setAppliedFilters(DashboardFiltersDTO appliedFilters) { this.appliedFilters = appliedFilters; }

    public static class BreakdownItemDTO {
        private Long id;
        private String label;
        private long count;

        public BreakdownItemDTO() {
        }

        public BreakdownItemDTO(Long id, String label, long count) {
            this.id = id;
            this.label = label;
            this.count = count;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class RecentPricelistDTO {
        private Long id;
        private String regionName;
        private String customerSegment;
        private PricelistStatus status;
        private String teamName;
        private int itemCount;
        private OffsetDateTime periodStart;
        private OffsetDateTime periodEnd;
        private OffsetDateTime lastEditedAt;
        private boolean creationCompleted;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }
        public String getCustomerSegment() { return customerSegment; }
        public void setCustomerSegment(String customerSegment) { this.customerSegment = customerSegment; }
        public PricelistStatus getStatus() { return status; }
        public void setStatus(PricelistStatus status) { this.status = status; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }
        public OffsetDateTime getPeriodStart() { return periodStart; }
        public void setPeriodStart(OffsetDateTime periodStart) { this.periodStart = periodStart; }
        public OffsetDateTime getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(OffsetDateTime periodEnd) { this.periodEnd = periodEnd; }
        public OffsetDateTime getLastEditedAt() { return lastEditedAt; }
        public void setLastEditedAt(OffsetDateTime lastEditedAt) { this.lastEditedAt = lastEditedAt; }
        public boolean isCreationCompleted() { return creationCompleted; }
        public void setCreationCompleted(boolean creationCompleted) { this.creationCompleted = creationCompleted; }
    }

    public static class DashboardFiltersDTO {
        private OffsetDateTime dateFrom;
        private OffsetDateTime dateTo;
        private Long teamId;
        private Long regionId;
        private PricelistStatus status;
        private String customerSegment;

        public OffsetDateTime getDateFrom() { return dateFrom; }
        public void setDateFrom(OffsetDateTime dateFrom) { this.dateFrom = dateFrom; }
        public OffsetDateTime getDateTo() { return dateTo; }
        public void setDateTo(OffsetDateTime dateTo) { this.dateTo = dateTo; }
        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }
        public Long getRegionId() { return regionId; }
        public void setRegionId(Long regionId) { this.regionId = regionId; }
        public PricelistStatus getStatus() { return status; }
        public void setStatus(PricelistStatus status) { this.status = status; }
        public String getCustomerSegment() { return customerSegment; }
        public void setCustomerSegment(String customerSegment) { this.customerSegment = customerSegment; }
    }
}
