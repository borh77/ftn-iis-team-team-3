package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Lead;
import com.example.iisdrugcrm.dto.sales.lead.LeadRequestDTO;
import com.example.iisdrugcrm.dto.sales.lead.LeadResponseDTO;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.LeadRepository;
import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.dto.sales.customer.CustomerResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;

    public LeadService(LeadRepository leadRepository, CustomerRepository customerRepository) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
    }

    public List<LeadResponseDTO> getAll() {
        return leadRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public LeadResponseDTO create(LeadRequestDTO dto) {
        if (leadRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Lead with this email already exists.");
        }

        Lead lead = new Lead(
                dto.getName(),
                dto.getEmail(),
                dto.getAddress(),
                dto.getSource(),
                dto.getScore()
        );

        return mapToDto(leadRepository.save(lead));
    }

    public LeadResponseDTO update(Long id, LeadRequestDTO dto) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        lead.update(dto.getName(), dto.getEmail(), dto.getAddress(), dto.getSource(), dto.getScore());

        return mapToDto(leadRepository.save(lead));
    }

    public LeadResponseDTO qualify(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        lead.qualify();

        return mapToDto(leadRepository.save(lead));
    }

    public CustomerResponseDTO convert(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        if (customerRepository.existsByEmail(lead.getEmail())) {
            throw new IllegalArgumentException("Customer with this email already exists.");
        }

        lead.convert();

        Customer customer = new Customer(
                lead.getName(),
                lead.getEmail(),
                null,
                null,
                lead.getAddress()
        );

        leadRepository.save(lead);
        Customer savedCustomer = customerRepository.save(customer);

        return mapCustomerToDto(savedCustomer);
    }

    private CustomerResponseDTO mapCustomerToDto(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getWebsite(),
                customer.getAddress(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private LeadResponseDTO mapToDto(Lead lead) {
        return new LeadResponseDTO(
                lead.getId(),
                lead.getName(),
                lead.getEmail(),
                lead.getAddress(),
                lead.getSource(),
                lead.getScore(),
                lead.getStatus(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}