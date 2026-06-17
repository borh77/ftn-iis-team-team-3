package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.communication.CommunicationRequestDTO;
import com.example.iisdrugcrm.dto.sales.communication.CommunicationResponseDTO;
import com.example.iisdrugcrm.service.sales.CustomerCommunicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/customers/{customerId}/communications")
public class CustomerCommunicationController {

    private final CustomerCommunicationService communicationService;

    public CustomerCommunicationController(CustomerCommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ACCOUNT_MANAGER')")
    public ResponseEntity<CommunicationResponseDTO> create(
            @PathVariable Long customerId,
            @Valid @RequestBody CommunicationRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(communicationService.create(customerId, dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ACCOUNT_MANAGER', 'SALES_REPRESENTATIVE', 'SALES_MANAGER')")
    public ResponseEntity<List<CommunicationResponseDTO>> getByCustomer(
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(communicationService.getByCustomer(customerId));
    }
}