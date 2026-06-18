package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;

public class VariantVersionStatusCountDTO {

    private VariantVersionStatus status;
    private long count;

    public VariantVersionStatusCountDTO(VariantVersionStatus status, long count) {
        this.status = status;
        this.count = count;
    }

    public VariantVersionStatus getStatus() {
        return status;
    }

    public long getCount() {
        return count;
    }
}