package com.example.iisdrugcrm.dto.sales.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSalesWorkflowRequest(
        @NotBlank(message = "Workflow name is required")
        @Size(max = 150)
        String name,

        Long regionId
) {
}