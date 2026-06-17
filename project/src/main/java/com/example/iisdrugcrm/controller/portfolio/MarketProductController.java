package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.MarketProductRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.MarketProductResponseDTO;
import com.example.iisdrugcrm.service.portfolio.MarketProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-products")
public class MarketProductController {

    private final MarketProductService marketProductService;

    public MarketProductController(MarketProductService marketProductService) {
        this.marketProductService = marketProductService;
    }

    @GetMapping
    public List<MarketProductResponseDTO> getMarketProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        return marketProductService.getMarketProducts(
                search,
                variantId,
                regionId,
                includeArchived
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public MarketProductResponseDTO create(
            @Valid @RequestBody MarketProductRequestDTO dto
    ) {
        return marketProductService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public MarketProductResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody MarketProductRequestDTO dto
    ) {
        return marketProductService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable Long id) {
        marketProductService.archive(id);
    }
}