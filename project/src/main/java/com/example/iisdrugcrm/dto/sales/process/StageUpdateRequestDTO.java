package com.example.iisdrugcrm.dto.sales.process;

import com.example.iisdrugcrm.domain.sales.SalesStage;
import jakarta.validation.constraints.NotNull;

public class StageUpdateRequestDTO {

    @NotNull
    private SalesStage stage;

    public SalesStage getStage() {
        return stage;
    }

    public void setStage(SalesStage stage) {
        this.stage = stage;
    }
}