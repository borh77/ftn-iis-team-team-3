package com.example.iisdrugcrm.controller.sales;

import com.example.iisdrugcrm.dto.sales.pricing.SalesPriceResponseDTO;
import com.example.iisdrugcrm.service.sales.SalesPricingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/pricing")
public class SalesPricingController {

    private final SalesPricingService salesPricingService;

    public SalesPricingController(SalesPricingService salesPricingService) {
        this.salesPricingService = salesPricingService;
    }

    @GetMapping("/price")
    @PreAuthorize("hasAnyRole('SALES_REPRESENTATIVE', 'SALES_MANAGER', 'ACCOUNT_MANAGER')")
    public SalesPriceResponseDTO getPrice(
            @RequestParam Long regionId,
            @RequestParam Long variantId,
            @RequestParam Integer quantity
    ) {
        return salesPricingService.getPrice(regionId, variantId, quantity);
    }
}