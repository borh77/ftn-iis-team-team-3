package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.domain.sales.SalesProcess;
import com.example.iisdrugcrm.domain.sales.SalesProcessHistory;
import com.example.iisdrugcrm.domain.sales.SalesStage;
import com.example.iisdrugcrm.dto.sales.process.CreateSalesProcessRequestDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessHistoryResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.StageUpdateRequestDTO;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessHistoryRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import com.example.iisdrugcrm.service.sales.workflow.SalesWorkflowService;
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
    public SalesProcessResponseDTO updateStage(Long id, StageUpdateRequestDTO dto, String username) {
        SalesProcess process = salesProcessRepository.findWithCustomerById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        SalesStage previousStage = process.getStage();

        if (!salesWorkflowService.isTransitionAllowed(previousStage, dto.getStage())) {
            throw new IllegalArgumentException(
                    "Transition from " + previousStage + " to " + dto.getStage() + " is not allowed by workflow."
            );
        }

        User changedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        process.changeStage(dto.getStage());

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
    public List<SalesStage> getAvailableTransitions(Long processId) {
        SalesProcess process = salesProcessRepository.findWithCustomerById(processId)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        return salesWorkflowService.getAvailableTransitions(process.getStage());
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