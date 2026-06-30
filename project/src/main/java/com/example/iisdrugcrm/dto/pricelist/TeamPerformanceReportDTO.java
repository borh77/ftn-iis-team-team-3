package com.example.iisdrugcrm.dto.pricelist;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class TeamPerformanceReportDTO {

    private Long teamId;
    private OffsetDateTime periodStart;
    private OffsetDateTime periodEnd;
    private BigDecimal averageTotalProcessingTimeHours;
    private BigDecimal averageReviewTimeHours;
    private Long activatedPricelistsCount;
    private Long stuckDraftCount;
    private Long stuckInReviewCount;
    private List<MonthlyPerformancePointDTO> monthlyTrend;
    private String teamFilterLimitation;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
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

    public BigDecimal getAverageTotalProcessingTimeHours() {
        return averageTotalProcessingTimeHours;
    }

    public void setAverageTotalProcessingTimeHours(BigDecimal averageTotalProcessingTimeHours) {
        this.averageTotalProcessingTimeHours = averageTotalProcessingTimeHours;
    }

    public BigDecimal getAverageReviewTimeHours() {
        return averageReviewTimeHours;
    }

    public void setAverageReviewTimeHours(BigDecimal averageReviewTimeHours) {
        this.averageReviewTimeHours = averageReviewTimeHours;
    }

    public Long getActivatedPricelistsCount() {
        return activatedPricelistsCount;
    }

    public void setActivatedPricelistsCount(Long activatedPricelistsCount) {
        this.activatedPricelistsCount = activatedPricelistsCount;
    }

    public Long getStuckDraftCount() {
        return stuckDraftCount;
    }

    public void setStuckDraftCount(Long stuckDraftCount) {
        this.stuckDraftCount = stuckDraftCount;
    }

    public Long getStuckInReviewCount() {
        return stuckInReviewCount;
    }

    public void setStuckInReviewCount(Long stuckInReviewCount) {
        this.stuckInReviewCount = stuckInReviewCount;
    }

    public List<MonthlyPerformancePointDTO> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<MonthlyPerformancePointDTO> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public String getTeamFilterLimitation() {
        return teamFilterLimitation;
    }

    public void setTeamFilterLimitation(String teamFilterLimitation) {
        this.teamFilterLimitation = teamFilterLimitation;
    }
}
