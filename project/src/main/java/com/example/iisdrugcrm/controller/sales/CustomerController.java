package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.customer.CustomerRequestDTO;
import com.example.iisdrugcrm.dto.sales.customer.CustomerResponseDTO;
import com.example.iisdrugcrm.service.sales.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public ResponseEntity<List<CustomerResponseDTO>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SALES_REPRESENTATIVE')")
    public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.update(id, dto));
    }
}