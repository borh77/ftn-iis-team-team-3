package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.pricelist.BuyerCatalogDTO;
import com.example.iisdrugcrm.service.BuyerCatalogService;
import com.example.iisdrugcrm.service.CatalogService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
@PreAuthorize("isAuthenticated()")
public class CatalogController {

    private final CatalogService catalogService;
    private final BuyerCatalogService buyerCatalogService;

    public CatalogController(CatalogService catalogService, BuyerCatalogService buyerCatalogService) {
        this.catalogService = catalogService;
        this.buyerCatalogService = buyerCatalogService;
    }

    @GetMapping("/variants")
    public List<CatalogVariantDTO> getActiveVariants() {
        return catalogService.getActiveVariants();
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    public BuyerCatalogDTO getBuyerCatalog(Authentication authentication) {
        return buyerCatalogService.getCatalogForBuyer(authentication.getName());
    }
}
