package com.example.iisdrugcrm.dto.sales.workflow;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSalesStageTransitionRequest(
        @NotNull(message = "From stage is required")
        Long fromStageId,

        @NotNull(message = "To stage is required")
        Long toStageId,

        @Size(max = 100)
        String conditionType,

        @Size(max = 500)
        String conditionDescription
) {
}