package com.example.iisdrugcrm.dto.pricelist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class SaveBasicInfoStepDTO {

    @NotNull(message = "Region is required")
    private Long regionId;

    @NotBlank(message = "Customer segment is required")
    @Size(max = 120, message = "Customer segment can contain at most 120 characters")
    private String customerSegment;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a three-letter ISO code")
    private String currency;

    @NotNull(message = "Period start is required")
    private OffsetDateTime periodStart;

    @NotNull(message = "Period end is required")
    private OffsetDateTime periodEnd;

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
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
}
