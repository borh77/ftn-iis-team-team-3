package com.example.iisdrugcrm.repository.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesStageDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesStageDefinitionRepository extends JpaRepository<SalesStageDefinition, Long> {

    List<SalesStageDefinition> findByWorkflow_IdOrderByStageOrderAsc(Long workflowId);
}