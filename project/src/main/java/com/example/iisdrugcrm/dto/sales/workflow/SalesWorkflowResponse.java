package com.example.iisdrugcrm.dto.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;

import java.time.LocalDateTime;
import java.util.List;

public record SalesWorkflowResponse(
        Long id,
        String name,
        Long regionId,
        String regionName,
        String regionCode,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SalesStageResponse> stages
) {
    public static SalesWorkflowResponse from(SalesWorkflow workflow) {
        return new SalesWorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.getRegion() != null ? workflow.getRegion().getId() : null,
                workflow.getRegion() != null ? workflow.getRegion().getName() : "GLOBAL",
                workflow.getRegion() != null ? workflow.getRegion().getCode() : "GLOBAL",
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