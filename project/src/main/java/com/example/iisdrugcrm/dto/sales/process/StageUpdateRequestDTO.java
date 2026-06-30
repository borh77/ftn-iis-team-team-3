package com.example.iisdrugcrm.dto.sales.process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StageUpdateRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String stage;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }
}