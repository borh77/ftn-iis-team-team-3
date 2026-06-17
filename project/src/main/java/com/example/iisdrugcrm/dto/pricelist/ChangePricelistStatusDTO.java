package com.example.iisdrugcrm.dto.pricelist;

import com.example.iisdrugcrm.domain.PricelistStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChangePricelistStatusDTO {

    @NotNull
    private PricelistStatus targetStatus;

    @Size(max = 500)
    private String reason;

    public PricelistStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(PricelistStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
