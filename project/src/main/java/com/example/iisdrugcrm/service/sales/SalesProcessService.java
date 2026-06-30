package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.domain.sales.SalesProcess;
import com.example.iisdrugcrm.domain.sales.SalesProcessHistory;
import com.example.iisdrugcrm.dto.sales.process.CreateSalesProcessRequestDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessHistoryResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.StageUpdateRequestDTO;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessHistoryRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import com.example.iisdrugcrm.service.sales.workflow.SalesWorkflowService;
import com.example.iisdrugcrm.domain.sales.workflow.SalesWorkflow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesProcessService {

    private final SalesProcessRepository salesProcessRepository;
    private final CustomerRepository customerRepository;
    private final SalesProcessHistoryRepository salesProcessHistoryRepository;
    private final SalesWorkflowService salesWorkflowService;
    private final UserRepository userRepository;

    public SalesProcessService(
            SalesProcessRepository salesProcessRepository,
            CustomerRepository customerRepository,
            SalesProcessHistoryRepository salesProcessHistoryRepository,
            SalesWorkflowService salesWorkflowService,
            UserRepository userRepository
    ) {
        this.salesProcessRepository = salesProcessRepository;
        this.customerRepository = customerRepository;
        this.salesProcessHistoryRepository = salesProcessHistoryRepository;
        this.salesWorkflowService = salesWorkflowService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SalesProcessResponseDTO> getAll() {
        return salesProcessRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public SalesProcessResponseDTO create(CreateSalesProcessRequestDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        SalesWorkflow workflow = salesWorkflowService.findActiveWorkflowEntity(dto.getWorkflowId());
        var startStage = salesWorkflowService.findStartStage(workflow.getId());

        SalesProcess salesProcess = new SalesProcess(
                customer,
                dto.getTitle(),
                workflow,
                startStage.name()
        );

        return mapToDto(salesProcessRepository.save(salesProcess));
    }

    @Transactional
    public SalesProcessResponseDTO updateStage(Long id, StageUpdateRequestDTO dto, String username) {
        SalesProcess process = salesProcessRepository.findWithCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        String previousStage = process.getStage();

        if (process.getWorkflow() == null) {
            throw new IllegalArgumentException("Sales process does not have a workflow assigned.");
        }

        if (!salesWorkflowService.isTransitionAllowed(process.getWorkflow().getId(), previousStage, dto.getStage())) {
            throw new IllegalArgumentException(
                    "Transition from " + previousStage + " to " + dto.getStage() + " is not allowed by workflow."
            );
        }

        User changedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        var targetStage = salesWorkflowService.findStageByName(process.getWorkflow().getId(), dto.getStage());
        
        process.changeStage(
                dto.getStage(),
                targetStage.endStage(),
                targetStage.successfulEnd()
        );

        salesProcessHistoryRepository.save(
                new SalesProcessHistory(process, previousStage, dto.getStage(), changedBy)
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
                        history.getChangedAt(),
                        history.getChangedBy() != null ? history.getChangedBy().getId() : null,
                        history.getChangedBy() != null ? history.getChangedBy().getUsername() : null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableTransitions(Long processId) {
        SalesProcess process = salesProcessRepository.findWithCustomerById(processId)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        if (process.getWorkflow() == null) {
                return List.of();
        }

        return salesWorkflowService.getAvailableTransitions(
                process.getWorkflow().getId(),
                process.getStage()
        );
    }

    private SalesProcessResponseDTO mapToDto(SalesProcess salesProcess) {
        return new SalesProcessResponseDTO(
                salesProcess.getId(),
                salesProcess.getCustomer().getId(),
                salesProcess.getCustomer().getName(),
                salesProcess.getTitle(),
                salesProcess.getWorkflow() != null ? salesProcess.getWorkflow().getId() : null,
                salesProcess.getWorkflow() != null ? salesProcess.getWorkflow().getName() : null,
                salesProcess.getStage(),
                salesProcess.getStatus(),
                salesProcess.getOutcome(),
                salesProcess.getCreatedAt(),
                salesProcess.getUpdatedAt()
        );
    }
}
