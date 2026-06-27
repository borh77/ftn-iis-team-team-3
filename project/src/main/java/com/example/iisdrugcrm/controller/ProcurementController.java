package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.order.ValidationResultDTO;
import com.example.iisdrugcrm.service.OrderValidationService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/procurement")
public class ProcurementController {

    private final OrderValidationService orderValidationService;

    public ProcurementController(OrderValidationService orderValidationService) {
        this.orderValidationService = orderValidationService;
    }

    @PostMapping(value = "/validation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BUYER')")
    public ValidationResultDTO validateOrderDocument(@RequestPart("file") MultipartFile file, Authentication authentication) {
        return orderValidationService.validateOrderDocument(authentication.getName(), file);
    }
}
