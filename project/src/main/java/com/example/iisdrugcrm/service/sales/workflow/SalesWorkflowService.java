package com.example.iisdrugcrm.service.sales.workflow;

import com.example.iisdrugcrm.domain.sales.workflow.SalesStageDefinition;
import com.example.iisdrugcrm.domain.sales.workflow.SalesStageTransition;
import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;
import com.example.iisdrugcrm.dto.sales.workflow.CreateSalesStageRequest;
import com.example.iisdrugcrm.dto.sales.workflow.CreateSalesStageTransitionRequest;
import com.example.iisdrugcrm.dto.sales.workflow.CreateSalesWorkflowRequest;
import com.example.iisdrugcrm.dto.sales.workflow.SalesStageResponse;
import com.example.iisdrugcrm.dto.sales.workflow.SalesStageTransitionResponse;
import com.example.iisdrugcrm.dto.sales.workflow.SalesWorkflowResponse;
import com.example.iisdrugcrm.repository.sales.workflow.SalesStageDefinitionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesStageTransitionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesWorkflowRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.iisdrugcrm.domain.sales.SalesStage;

import java.util.Comparator;
import java.util.List;

@Service
public class SalesWorkflowService {

    private final SalesWorkflowRepository workflowRepository;
    private final SalesStageDefinitionRepository stageRepository;
    private final SalesStageTransitionRepository transitionRepository;

    public SalesWorkflowService(
            SalesWorkflowRepository workflowRepository,
            SalesStageDefinitionRepository stageRepository,
            SalesStageTransitionRepository transitionRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.stageRepository = stageRepository;
        this.transitionRepository = transitionRepository;
    }

    @Transactional(readOnly = true)
    public List<SalesWorkflowResponse> getActiveWorkflows() {
        return workflowRepository.findByActiveTrue()
                .stream()
                .map(SalesWorkflowResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesWorkflowResponse getWorkflow(Long workflowId) {
        SalesWorkflow workflow = findWorkflow(workflowId);
        return SalesWorkflowResponse.from(workflow);
    }

    @Transactional
    public SalesWorkflowResponse createWorkflow(CreateSalesWorkflowRequest request) {
        SalesWorkflow workflow = new SalesWorkflow(request.name(), request.region());
        return SalesWorkflowResponse.from(workflowRepository.save(workflow));
    }

    @Transactional
    public SalesStageResponse addStage(Long workflowId, CreateSalesStageRequest request) {
        SalesWorkflow workflow = findWorkflow(workflowId);

        validateStageFlags(workflowId, request);

        SalesStageDefinition stage = new SalesStageDefinition(
                workflow,
                request.name(),
                request.description(),
                request.stageOrder(),
                request.startStage(),
                request.endStage(),
                request.successfulEnd(),
                request.requiredInputs(),
                request.expectedOutputs()
        );

        return SalesStageResponse.from(stageRepository.save(stage));
    }

    @Transactional(readOnly = true)
    public List<SalesStageResponse> getStages(Long workflowId) {
        return stageRepository.findByWorkflow_IdOrderByStageOrderAsc(workflowId)
                .stream()
                .map(SalesStageResponse::from)
                .toList();
    }

    @Transactional
    public SalesStageTransitionResponse addTransition(
            Long workflowId,
            CreateSalesStageTransitionRequest request
    ) {
        SalesWorkflow workflow = findWorkflow(workflowId);

        SalesStageDefinition fromStage = findStage(request.fromStageId());
        SalesStageDefinition toStage = findStage(request.toStageId());

        if (!fromStage.getWorkflow().getId().equals(workflowId)
                || !toStage.getWorkflow().getId().equals(workflowId)) {
            throw new IllegalArgumentException("Both stages must belong to the selected workflow");
        }

        transitionRepository
                .findByWorkflow_IdAndFromStage_IdAndToStage_Id(
                        workflowId,
                        request.fromStageId(),
                        request.toStageId()
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Transition already exists");
                });

        SalesStageTransition transition = new SalesStageTransition(
                workflow,
                fromStage,
                toStage,
                request.conditionType(),
                request.conditionDescription()
        );

        return SalesStageTransitionResponse.from(transitionRepository.save(transition));
    }

    @Transactional(readOnly = true)
    public List<SalesStageTransitionResponse> getTransitions(Long workflowId) {
        return transitionRepository.findByWorkflow_Id(workflowId)
                .stream()
                .sorted(Comparator.comparing(t -> t.getFromStage().getStageOrder()))
                .map(SalesStageTransitionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isTransitionAllowed(SalesStage currentStage, SalesStage targetStage) {
        SalesWorkflow workflow = findActiveWorkflowByRegion("GLOBAL");

        SalesStageDefinition fromStage = findStageDefinition(workflow.getId(), currentStage);
        SalesStageDefinition toStage = findStageDefinition(workflow.getId(), targetStage);

        return transitionRepository
                .findByWorkflow_IdAndFromStage_IdAndToStage_Id(
                        workflow.getId(),
                        fromStage.getId(),
                        toStage.getId()
                )
                .isPresent();
    }

    @Transactional(readOnly = true)
    public List<SalesStage> getAvailableTransitions(SalesStage currentStage) {
        SalesWorkflow workflow = findActiveWorkflowByRegion("GLOBAL");

        SalesStageDefinition fromStage = findStageDefinition(workflow.getId(), currentStage);

        return transitionRepository.findByWorkflow_Id(workflow.getId())
                .stream()
                .filter(transition -> transition.getFromStage().getId().equals(fromStage.getId()))
                .map(transition -> mapWorkflowStageNameToEnumStage(transition.getToStage().getName()))
                .toList();
    }

    private SalesWorkflow findWorkflow(Long workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Sales workflow not found"));
    }

    private SalesStageDefinition findStage(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Sales stage not found"));
    }

    private void validateStageFlags(Long workflowId, CreateSalesStageRequest request) {
        List<SalesStageDefinition> existingStages =
                stageRepository.findByWorkflow_IdOrderByStageOrderAsc(workflowId);

        if (request.startStage() && existingStages.stream().anyMatch(SalesStageDefinition::isStartStage)) {
            throw new IllegalArgumentException("Workflow can have only one start stage");
        }

        if (request.successfulEnd() && !request.endStage()) {
            throw new IllegalArgumentException("Successful end stage must also be marked as end stage");
        }
    }

    private SalesWorkflow findActiveWorkflowByRegion(String region) {
        return workflowRepository.findByRegionAndActiveTrue(region)
                .orElseThrow(() -> new IllegalStateException(
                        "Active sales workflow for region " + region + " not found."
                ));
    }

    private SalesStageDefinition findStageDefinition(Long workflowId, SalesStage stage) {
        String stageName = mapEnumStageToWorkflowStageName(stage);

        return stageRepository.findByWorkflow_IdOrderByStageOrderAsc(workflowId)
                .stream()
                .filter(definition -> definition.getName().equals(stageName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Stage " + stageName + " is not defined in active workflow."
                ));
    }

    private String mapEnumStageToWorkflowStageName(SalesStage stage) {
        return switch (stage) {
            case NEW -> "New";
            case CONTACTED -> "Contacted";
            case QUALIFIED -> "Qualified";
            case PROPOSAL_SENT -> "Proposal Sent";
            case NEGOTIATION -> "Negotiation";
            case WON -> "Closed Won";
            case LOST -> "Closed Lost";
        };
    }

    private SalesStage mapWorkflowStageNameToEnumStage(String stageName) {
        return switch (stageName) {
            case "New" -> SalesStage.NEW;
            case "Contacted" -> SalesStage.CONTACTED;
            case "Qualified" -> SalesStage.QUALIFIED;
            case "Proposal Sent" -> SalesStage.PROPOSAL_SENT;
            case "Negotiation" -> SalesStage.NEGOTIATION;
            case "Closed Won" -> SalesStage.WON;
            case "Closed Lost" -> SalesStage.LOST;
            default -> throw new IllegalStateException("Unknown workflow stage name: " + stageName);
        };
    }
}