package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseStatus;
import jakarta.validation.constraints.NotNull;

public class MarketLicenseStatusRequestDTO {

    @NotNull
    private MarketLicenseStatus status;

    public MarketLicenseStatus getStatus() {
        return status;
    }

    public void setStatus(MarketLicenseStatus status) {
        this.status = status;
    }
}