package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseStatus;

public class MarketLicenseStatusCountDTO {

    private MarketLicenseStatus status;
    private long count;

    public MarketLicenseStatusCountDTO(MarketLicenseStatus status, long count) {
        this.status = status;
        this.count = count;
    }

    public MarketLicenseStatus getStatus() {
        return status;
    }

    public long getCount() {
        return count;
    }
}