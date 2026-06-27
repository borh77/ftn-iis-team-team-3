package com.example.iisdrugcrm.dto.pricelist;

import java.math.BigDecimal;

public class MonthlyPerformancePointDTO {

    private String month;
    private BigDecimal averageTotalProcessingTimeHours;
    private Long activatedPricelistsCount;

    public MonthlyPerformancePointDTO() {
    }

    public MonthlyPerformancePointDTO(String month, BigDecimal averageTotalProcessingTimeHours, Long activatedPricelistsCount) {
        this.month = month;
        this.averageTotalProcessingTimeHours = averageTotalProcessingTimeHours;
        this.activatedPricelistsCount = activatedPricelistsCount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getAverageTotalProcessingTimeHours() {
        return averageTotalProcessingTimeHours;
    }

    public void setAverageTotalProcessingTimeHours(BigDecimal averageTotalProcessingTimeHours) {
        this.averageTotalProcessingTimeHours = averageTotalProcessingTimeHours;
    }

    public Long getActivatedPricelistsCount() {
        return activatedPricelistsCount;
    }

    public void setActivatedPricelistsCount(Long activatedPricelistsCount) {
        this.activatedPricelistsCount = activatedPricelistsCount;
    }
}
