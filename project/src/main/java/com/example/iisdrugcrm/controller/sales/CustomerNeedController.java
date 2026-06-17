package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.need.CustomerNeedRequestDTO;
import com.example.iisdrugcrm.dto.sales.need.CustomerNeedResponseDTO;
import com.example.iisdrugcrm.service.sales.CustomerNeedService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/needs")
public class CustomerNeedController {

    private final CustomerNeedService customerNeedService;

    public CustomerNeedController(CustomerNeedService customerNeedService) {
        this.customerNeedService = customerNeedService;
    }

    @PostMapping
    public CustomerNeedResponseDTO create(@PathVariable Long customerId,
                                          @Valid @RequestBody CustomerNeedRequestDTO dto) {
        return customerNeedService.create(customerId, dto);
    }

    @GetMapping
    public List<CustomerNeedResponseDTO> getByCustomer(@PathVariable Long customerId) {
        return customerNeedService.getByCustomer(customerId);
    }
}