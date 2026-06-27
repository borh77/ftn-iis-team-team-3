package com.example.iisdrugcrm.dto.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;

import java.time.LocalDateTime;
import java.util.List;

public record SalesWorkflowResponse(
        Long id,
        String name,
        String region,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SalesStageResponse> stages
) {
    public static SalesWorkflowResponse from(SalesWorkflow workflow) {
        return new SalesWorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getRegion(),
                workflow.isActive(),
                workflow.getCreatedAt(),
                workflow.getUpdatedAt(),
                workflow.getStages()
                        .stream()
                        .map(SalesStageResponse::from)
                        .toList()
        );
    }
}