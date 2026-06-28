package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Lead;
import com.example.iisdrugcrm.dto.sales.lead.LeadRequestDTO;
import com.example.iisdrugcrm.dto.sales.lead.LeadResponseDTO;
import com.example.iisdrugcrm.repository.RegionRepository;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.repository.sales.LeadRepository;

import org.springframework.transaction.annotation.Transactional;

import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.dto.sales.customer.CustomerResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final RegionRepository regionRepository;

    public LeadService(LeadRepository leadRepository, CustomerRepository customerRepository, RegionRepository regionRepository) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.regionRepository = regionRepository;
    }

    @Transactional(readOnly = true)
    public List<LeadResponseDTO> getAll() {
        return leadRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public LeadResponseDTO create(LeadRequestDTO dto) {
        if (leadRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Lead with this email already exists.");
        }

        Region region = resolveRegion(dto.getRegionId());

        Lead lead = new Lead(
            dto.getName(),
            dto.getEmail(),
            dto.getAddress(),
            region,
            dto.getSource(),
            dto.getScore()
        );

        return mapToDto(leadRepository.save(lead));
    }

    @Transactional
    public LeadResponseDTO update(Long id, LeadRequestDTO dto) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        Region region = resolveRegion(dto.getRegionId());

        lead.update(
            dto.getName(),
            dto.getEmail(),
            dto.getAddress(),
            region,
            dto.getSource(),
            dto.getScore()
        );

        return mapToDto(leadRepository.save(lead));
    }

    @Transactional
    public LeadResponseDTO qualify(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found."));

        lead.qualify();

        return mapToDto(leadRepository.save(lead));
    }

    @Transactional
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
            lead.getAddress(),
            lead.getRegion()
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
                customer.getRegion() != null ? customer.getRegion().getId() : null,
                customer.getRegion() != null ? customer.getRegion().getName() : null,
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
                lead.getUpdatedAt(),
                lead.getRegion() != null ? lead.getRegion().getId() : null,
                lead.getRegion() != null ? lead.getRegion().getName() : null
        );
    }
    
    private Region resolveRegion(Long regionId) {
        if (regionId == null) {
            return null;
        }

        return regionRepository.findById(regionId)
            .orElseThrow(() -> new IllegalArgumentException("Region not found."));
    }
}