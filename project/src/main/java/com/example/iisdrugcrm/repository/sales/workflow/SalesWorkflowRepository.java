package com.example.iisdrugcrm.repository.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesWorkflowRepository extends JpaRepository<SalesWorkflow, Long> {

    List<SalesWorkflow> findByActiveTrue();

    Optional<SalesWorkflow> findByIdAndActiveTrue(Long id);

    Optional<SalesWorkflow> findByRegion_IdAndActiveTrue(Long regionId);

    Optional<SalesWorkflow> findByRegionIsNullAndActiveTrue();
}