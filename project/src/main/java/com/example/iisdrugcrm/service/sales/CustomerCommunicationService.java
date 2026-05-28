package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.domain.sales.CustomerCommunication;
import com.example.iisdrugcrm.dto.sales.communication.CommunicationRequestDTO;
import com.example.iisdrugcrm.dto.sales.communication.CommunicationResponseDTO;
import com.example.iisdrugcrm.repository.sales.CustomerCommunicationRepository;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerCommunicationService {

    private final CustomerCommunicationRepository communicationRepository;
    private final CustomerRepository customerRepository;

    public CustomerCommunicationService(CustomerCommunicationRepository communicationRepository,
                                        CustomerRepository customerRepository) {
        this.communicationRepository = communicationRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CommunicationResponseDTO create(Long customerId, CommunicationRequestDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        CustomerCommunication communication = new CustomerCommunication(
                customer,
                dto.getType(),
                dto.getCommunicationDate(),
                dto.getSummary()
        );

        return mapToDto(communicationRepository.save(communication));
    }

    @Transactional(readOnly = true)
    public List<CommunicationResponseDTO> getByCustomer(Long customerId) {
        return communicationRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private CommunicationResponseDTO mapToDto(CustomerCommunication communication) {
        return new CommunicationResponseDTO(
                communication.getId(),
                communication.getCustomer().getId(),
                communication.getCustomer().getName(),
                communication.getType(),
                communication.getCommunicationDate(),
                communication.getSummary()
        );
    }
}