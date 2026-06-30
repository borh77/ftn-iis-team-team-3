package com.example.iisdrugcrm.dto.sales.workflow;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSalesStageRequest(
        @NotBlank(message = "Stage name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @NotNull(message = "Stage order is required")
        @Min(1)
        Integer stageOrder,

        boolean startStage,
        boolean endStage,
        boolean successfulEnd,

        String requiredInputs,
        String expectedOutputs
) {
}