package com.example.iisdrugcrm.dto.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesStageTransition;

public record SalesStageTransitionResponse(
        Long id,
        Long workflowId,
        Long fromStageId,
        String fromStageName,
        Long toStageId,
        String toStageName,
        String conditionType,
        String conditionDescription
) {
    public static SalesStageTransitionResponse from(SalesStageTransition transition) {
        return new SalesStageTransitionResponse(
                transition.getId(),
                transition.getWorkflow().getId(),
                transition.getFromStage().getId(),
                transition.getFromStage().getName(),
                transition.getToStage().getId(),
                transition.getToStage().getName(),
                transition.getConditionType(),
                transition.getConditionDescription()
        );
    }
}