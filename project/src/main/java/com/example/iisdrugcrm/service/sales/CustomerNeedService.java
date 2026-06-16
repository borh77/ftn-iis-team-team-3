package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.domain.sales.CustomerNeed;
import com.example.iisdrugcrm.domain.sales.SalesProcess;
import com.example.iisdrugcrm.dto.sales.need.CustomerNeedRequestDTO;
import com.example.iisdrugcrm.dto.sales.need.CustomerNeedResponseDTO;
import com.example.iisdrugcrm.repository.sales.CustomerNeedRepository;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerNeedService {

    private final CustomerNeedRepository customerNeedRepository;
    private final CustomerRepository customerRepository;
    private final SalesProcessRepository salesProcessRepository;

    public CustomerNeedService(CustomerNeedRepository customerNeedRepository,
                               CustomerRepository customerRepository,
                               SalesProcessRepository salesProcessRepository) {
        this.customerNeedRepository = customerNeedRepository;
        this.customerRepository = customerRepository;
        this.salesProcessRepository = salesProcessRepository;
    }

    @Transactional
    public CustomerNeedResponseDTO create(Long customerId, CustomerNeedRequestDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        SalesProcess salesProcess = salesProcessRepository.findWithCustomerById(dto.getSalesProcessId())
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        if (!salesProcess.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Sales process does not belong to selected customer.");
        }

        CustomerNeed need = new CustomerNeed(
                customer,
                salesProcess,
                dto.getDescription(),
                dto.getPriority()
        );

        return mapToDto(customerNeedRepository.save(need));
    }

    @Transactional(readOnly = true)
    public List<CustomerNeedResponseDTO> getByCustomer(Long customerId) {
        return customerNeedRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerNeedResponseDTO> getBySalesProcess(Long salesProcessId) {
        return customerNeedRepository.findBySalesProcessId(salesProcessId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private CustomerNeedResponseDTO mapToDto(CustomerNeed need) {
        return new CustomerNeedResponseDTO(
                need.getId(),
                need.getCustomer().getId(),
                need.getCustomer().getName(),
                need.getSalesProcess().getId(),
                need.getSalesProcess().getTitle(),
                need.getDescription(),
                need.getPriority(),
                need.getCreatedAt()
        );
    }
}