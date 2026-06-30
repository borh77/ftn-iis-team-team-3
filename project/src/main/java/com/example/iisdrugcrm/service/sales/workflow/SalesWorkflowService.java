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
import com.example.iisdrugcrm.repository.RegionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesStageDefinitionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesStageTransitionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesWorkflowRepository;
import com.example.iisdrugcrm.domain.Region;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SalesWorkflowService {

    private final SalesWorkflowRepository workflowRepository;
    private final SalesStageDefinitionRepository stageRepository;
    private final SalesStageTransitionRepository transitionRepository;
    private final RegionRepository regionRepository;

    public SalesWorkflowService(
            SalesWorkflowRepository workflowRepository,
            SalesStageDefinitionRepository stageRepository,
            SalesStageTransitionRepository transitionRepository,
            RegionRepository regionRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.stageRepository = stageRepository;
        this.transitionRepository = transitionRepository;
        this.regionRepository = regionRepository;
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
        Region region = null;

        if (request.regionId() != null) {
            region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new IllegalArgumentException("Region not found."));
        }

        SalesWorkflow workflow = new SalesWorkflow(request.name(), region);
        return SalesWorkflowResponse.from(workflowRepository.save(workflow));
    }

    @Transactional
    public SalesStageResponse addStage(Long workflowId, CreateSalesStageRequest request) {
        SalesWorkflow workflow = findWorkflow(workflowId);

        validateStageFlags(workflowId, request);

        SalesStageDefinition stage = new SalesStageDefinition(
                workflow,
                request.name().trim(),
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
    public boolean isTransitionAllowed(Long workflowId, String currentStage, String targetStage) {
        SalesWorkflow workflow = findActiveWorkflowEntity(workflowId);

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
    public List<String> getAvailableTransitions(Long workflowId, String currentStage) {
        SalesWorkflow workflow = findActiveWorkflowEntity(workflowId);

        SalesStageDefinition fromStage = findStageDefinition(workflow.getId(), currentStage);

        return transitionRepository.findByWorkflow_Id(workflow.getId())
                .stream()
                .filter(transition -> transition.getFromStage().getId().equals(fromStage.getId()))
                .map(transition -> transition.getToStage().getName())
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesStageResponse findStageByName(Long workflowId, String stageName) {
        SalesWorkflow workflow = findActiveWorkflowEntity(workflowId);        
        
        SalesStageDefinition stage = findStageDefinition(workflow.getId(), stageName);
        return SalesStageResponse.from(stage);
    }


    private SalesWorkflow findWorkflow(Long workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Sales workflow not found"));
    }

    private SalesStageDefinition findStage(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Sales stage not found"));
    }

    private SalesStageDefinition findStageDefinition(Long workflowId, String stageName) {
        return stageRepository.findByWorkflow_IdOrderByStageOrderAsc(workflowId)
                .stream()
                .filter(definition -> definition.getName().equalsIgnoreCase(stageName.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Stage " + stageName + " is not defined in active workflow."
                ));
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

    @Transactional(readOnly = true)
    public SalesWorkflow findActiveWorkflowEntity(Long workflowId) {
        return workflowRepository.findByIdAndActiveTrue(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Active sales workflow not found."));
    }

    @Transactional(readOnly = true)
    public SalesStageResponse findStartStage(Long workflowId) {
        SalesStageDefinition startStage = stageRepository.findByWorkflow_IdOrderByStageOrderAsc(workflowId)
                .stream()
                .filter(SalesStageDefinition::isStartStage)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Selected workflow does not have a start stage."));

        return SalesStageResponse.from(startStage);
    }
}