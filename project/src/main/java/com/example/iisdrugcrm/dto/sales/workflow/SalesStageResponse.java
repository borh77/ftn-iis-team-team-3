package com.example.iisdrugcrm.dto.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesStageDefinition;

public record SalesStageResponse(
        Long id,
        Long workflowId,
        String name,
        String description,
        Integer stageOrder,
        boolean startStage,
        boolean endStage,
        boolean successfulEnd,
        String requiredInputs,
        String expectedOutputs
) {
    public static SalesStageResponse from(SalesStageDefinition stage) {
        return new SalesStageResponse(
                stage.getId(),
                stage.getWorkflow().getId(),
                stage.getName(),
                stage.getDescription(),
                stage.getStageOrder(),
                stage.isStartStage(),
                stage.isEndStage(),
                stage.isSuccessfulEnd(),
                stage.getRequiredInputs(),
                stage.getExpectedOutputs()
        );
    }
}