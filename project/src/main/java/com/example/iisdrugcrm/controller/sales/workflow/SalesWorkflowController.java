package com.example.iisdrugcrm.controller.sales.workflow;

import com.example.iisdrugcrm.dto.sales.workflow.CreateSalesStageRequest;
import com.example.iisdrugcrm.dto.sales.workflow.CreateSalesStageTransitionRequest;
import com.example.iisdrugcrm.dto.sales.workflow.CreateSalesWorkflowRequest;
import com.example.iisdrugcrm.dto.sales.workflow.SalesStageResponse;
import com.example.iisdrugcrm.dto.sales.workflow.SalesStageTransitionResponse;
import com.example.iisdrugcrm.dto.sales.workflow.SalesWorkflowResponse;
import com.example.iisdrugcrm.service.sales.workflow.SalesWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/workflows")
public class SalesWorkflowController {

    private final SalesWorkflowService workflowService;

    public SalesWorkflowController(SalesWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public List<SalesWorkflowResponse> getWorkflows() {
        return workflowService.getActiveWorkflows();
    }

    @GetMapping("/{workflowId}")
    public SalesWorkflowResponse getWorkflow(@PathVariable Long workflowId) {
        return workflowService.getWorkflow(workflowId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesWorkflowResponse createWorkflow(
            @Valid @RequestBody CreateSalesWorkflowRequest request
    ) {
        return workflowService.createWorkflow(request);
    }

    @GetMapping("/{workflowId}/stages")
    public List<SalesStageResponse> getStages(
            @PathVariable Long workflowId
    ) {
        return workflowService.getStages(workflowId);
    }

    @PostMapping("/{workflowId}/stages")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesStageResponse addStage(
            @PathVariable Long workflowId,
            @Valid @RequestBody CreateSalesStageRequest request
    ) {
        return workflowService.addStage(workflowId, request);
    }

    @GetMapping("/{workflowId}/transitions")
    public List<SalesStageTransitionResponse> getTransitions(
            @PathVariable Long workflowId
    ) {
        return workflowService.getTransitions(workflowId);
    }

    @PostMapping("/{workflowId}/transitions")
    @ResponseStatus(HttpStatus.CREATED)
    public SalesStageTransitionResponse addTransition(
            @PathVariable Long workflowId,
            @Valid @RequestBody CreateSalesStageTransitionRequest request
    ) {
        return workflowService.addTransition(workflowId, request);
    }
}