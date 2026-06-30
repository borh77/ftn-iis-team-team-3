package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.dto.sales.customer.CustomerRequestDTO;
import com.example.iisdrugcrm.dto.sales.customer.CustomerResponseDTO;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.repository.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RegionRepository regionRepository;

    public CustomerService(CustomerRepository customerRepository, RegionRepository regionRepository) {
        this.customerRepository = customerRepository;
        this.regionRepository = regionRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Customer with this email already exists.");
        }
        Region region = resolveRegion(dto.getRegionId());

        Customer customer = new Customer(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getWebsite(),
                dto.getAddress(),
                region
        );

        return mapToDto(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponseDTO update(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        Region region = resolveRegion(dto.getRegionId());

        customer.update(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getWebsite(),
                dto.getAddress(),
                region
        );

        return mapToDto(customerRepository.save(customer));
    }

    private CustomerResponseDTO mapToDto(Customer customer) {
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

    private Region resolveRegion(Long regionId) {
        if (regionId == null) {
            return null;
        }

        return regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found."));
    }
}