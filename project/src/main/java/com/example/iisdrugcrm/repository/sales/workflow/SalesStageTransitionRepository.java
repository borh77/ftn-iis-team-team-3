package com.example.iisdrugcrm.repository.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesStageTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesStageTransitionRepository extends JpaRepository<SalesStageTransition, Long> {

    List<SalesStageTransition> findByWorkflow_Id(Long workflowId);

    Optional<SalesStageTransition> findByWorkflow_IdAndFromStage_IdAndToStage_Id(
            Long workflowId,
            Long fromStageId,
            Long toStageId
    );
}