package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.domain.sales.SalesProcess;
import com.example.iisdrugcrm.dto.sales.process.CreateSalesProcessRequestDTO;
import com.example.iisdrugcrm.dto.sales.process.SalesProcessResponseDTO;
import com.example.iisdrugcrm.dto.sales.process.StageUpdateRequestDTO;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalesProcessService {

    private final SalesProcessRepository salesProcessRepository;
    private final CustomerRepository customerRepository;

    public SalesProcessService(SalesProcessRepository salesProcessRepository,
                               CustomerRepository customerRepository) {
        this.salesProcessRepository = salesProcessRepository;
        this.customerRepository = customerRepository;
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

        process.changeStage(dto.getStage());

        return mapToDto(process);
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