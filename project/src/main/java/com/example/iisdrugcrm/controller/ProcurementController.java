package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementRequestDTO;
import com.example.iisdrugcrm.dto.procurement.ProcurementOrderResponseDTO;
import com.example.iisdrugcrm.dto.order.ValidationResultDTO;
import com.example.iisdrugcrm.service.OrderValidationService;
import com.example.iisdrugcrm.service.ProcurementOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/procurement")
public class ProcurementController {

    private final OrderValidationService orderValidationService;
    private final ProcurementOrderService procurementOrderService;

    public ProcurementController(OrderValidationService orderValidationService, ProcurementOrderService procurementOrderService) {
        this.orderValidationService = orderValidationService;
        this.procurementOrderService = procurementOrderService;
    }

    @PostMapping(value = "/validation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUYER')")
    public ValidationResultDTO validateOrderDocument(@RequestPart("file") MultipartFile file, Authentication authentication) {
        return orderValidationService.validateOrderDocument(authentication.getName(), file);
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ProcurementOrderResponseDTO> confirmOrder(
            @Valid @RequestBody ConfirmProcurementRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(procurementOrderService.confirm(authentication.getName(), request));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('BUYER')")
    public List<ProcurementOrderResponseDTO> listMyOrders(Authentication authentication) {
        return procurementOrderService.listMine(authentication.getName());
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public ProcurementOrderResponseDTO getMyOrder(@PathVariable Long id, Authentication authentication) {
        return procurementOrderService.getMine(authentication.getName(), id);
    }
}
