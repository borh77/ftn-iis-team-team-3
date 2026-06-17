package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.Customer;
import com.example.iisdrugcrm.dto.sales.customer.CustomerRequestDTO;
import com.example.iisdrugcrm.dto.sales.customer.CustomerResponseDTO;
import com.example.iisdrugcrm.repository.sales.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponseDTO> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public CustomerResponseDTO create(CustomerRequestDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Customer with this email already exists.");
        }

        Customer customer = new Customer(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getWebsite(),
                dto.getAddress()
        );

        return mapToDto(customerRepository.save(customer));
    }

    public CustomerResponseDTO update(Long id, CustomerRequestDTO dto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        customer.update(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getWebsite(),
                dto.getAddress()
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
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}