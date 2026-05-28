package com.example.iisdrugcrm.dto.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;
import jakarta.validation.constraints.NotNull;

public class VariantVersionStatusRequestDTO {

    @NotNull
    private VariantVersionStatus status;

    public VariantVersionStatus getStatus() {
        return status;
    }
}