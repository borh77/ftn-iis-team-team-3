package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.domain.sales.SalesProcess;
import com.example.iisdrugcrm.dto.sales.process.CreateSalesProcessRequestDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessHistoryResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.StageUpdateRequestDTO;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesStageDefinitionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesStageTransitionRepository;
import com.example.iisdrugcrm.repository.sales.workflow.SalesWorkflowRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.iisdrugcrm.domain.sales.SalesProcessHistory;
import com.example.iisdrugcrm.domain.sales.SalesStage;
import com.example.iisdrugcrm.domain.sales.workflow.SalesStageDefinition;
import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;
import com.example.iisdrugcrm.repository.sales.SalesProcessHistoryRepository;


import java.util.List;

@Service
public class SalesProcessService {

    private final SalesProcessRepository salesProcessRepository;
    private final CustomerRepository customerRepository;
    private final SalesProcessHistoryRepository salesProcessHistoryRepository;
    private final SalesWorkflowRepository salesWorkflowRepository;
    private final SalesStageDefinitionRepository salesStageDefinitionRepository;
    private final SalesStageTransitionRepository salesStageTransitionRepository;

    public SalesProcessService(
            SalesProcessRepository salesProcessRepository,
            CustomerRepository customerRepository,
            SalesProcessHistoryRepository salesProcessHistoryRepository,
            SalesWorkflowRepository salesWorkflowRepository,
            SalesStageDefinitionRepository salesStageDefinitionRepository,
            SalesStageTransitionRepository salesStageTransitionRepository
    ) {
        this.salesProcessRepository = salesProcessRepository;
        this.customerRepository = customerRepository;
        this.salesProcessHistoryRepository = salesProcessHistoryRepository;
        this.salesWorkflowRepository = salesWorkflowRepository;
        this.salesStageDefinitionRepository = salesStageDefinitionRepository;
        this.salesStageTransitionRepository = salesStageTransitionRepository;
    }

    public List<SalesProcessResponseDTO> getAll() {
        return salesProcessRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public SalesProcessResponseDTO create(CreateSalesProcessRequestDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        SalesProcess salesProcess = new SalesProcess(customer, dto.getTitle());

        return mapToDto(salesProcessRepository.save(salesProcess));
    }

    @Transactional
    public SalesProcessResponseDTO updateStage(Long id, StageUpdateRequestDTO dto) {
        SalesProcess process = salesProcessRepository.findWithCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        SalesStage previousStage = process.getStage();

        validateWorkflowTransition(previousStage, dto.getStage());
        
        process.changeStage(dto.getStage());

        salesProcessHistoryRepository.save(
                new SalesProcessHistory(process, previousStage, dto.getStage())
        );

        return mapToDto(process);
    }

    @Transactional(readOnly = true)
    public SalesProcessResponseDTO getById(Long id) {
        SalesProcess process = salesProcessRepository.findWithCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        return mapToDto(process);
    }

    @Transactional(readOnly = true)
    public List<SalesProcessHistoryResponseDTO> getHistory(Long id) {
        return salesProcessHistoryRepository.findBySalesProcess_IdOrderByChangedAtDesc(id)
                .stream()
                .map(history -> new SalesProcessHistoryResponseDTO(
                        history.getId(),
                        history.getSalesProcess().getId(),
                        history.getPreviousStage(),
                        history.getNewStage(),
                        history.getChangedAt()
                ))
                .toList();
    }

    private void validateWorkflowTransition(SalesStage currentStage, SalesStage targetStage) {
        SalesWorkflow workflow = salesWorkflowRepository.findByRegionAndActiveTrue("GLOBAL")
                .orElseThrow(() -> new IllegalStateException("Active sales workflow for GLOBAL region not found."));

        SalesStageDefinition fromStage = findStageDefinition(workflow.getId(), currentStage);
        SalesStageDefinition toStage = findStageDefinition(workflow.getId(), targetStage);

        salesStageTransitionRepository
                .findByWorkflow_IdAndFromStage_IdAndToStage_Id(
                        workflow.getId(),
                        fromStage.getId(),
                        toStage.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transition from " + currentStage + " to " + targetStage + " is not allowed by workflow."
                ));
    }

    private SalesStageDefinition findStageDefinition(Long workflowId, SalesStage stage) {
        String stageName = mapEnumStageToWorkflowStageName(stage);

        return salesStageDefinitionRepository.findByWorkflow_IdOrderByStageOrderAsc(workflowId)
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

    private SalesProcessResponseDTO mapToDto(SalesProcess salesProcess) {
        return new SalesProcessResponseDTO(
                salesProcess.getId(),
                salesProcess.getCustomer().getId(),
                salesProcess.getCustomer().getName(),
                salesProcess.getTitle(),
                salesProcess.getStage(),
                salesProcess.getStatus(),
                salesProcess.getOutcome(),
                salesProcess.getCreatedAt(),
                salesProcess.getUpdatedAt()
        );
    }
}