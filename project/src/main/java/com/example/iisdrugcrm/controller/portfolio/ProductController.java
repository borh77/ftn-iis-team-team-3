package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.ProductResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.ProductRequestDTO;
import com.example.iisdrugcrm.service.portfolio.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponseDTO> getProducts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long subcategoryId,
        @RequestParam(required = false) Long therapeuticAreaId,
        @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        return productService.getProducts(
            search,
            subcategoryId,
            therapeuticAreaId,
            includeArchived
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public ProductResponseDTO create(@Valid @RequestBody ProductRequestDTO dto) {
        return productService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public ProductResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto
    ) {
        return productService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public void archive(@PathVariable Long id) {
        productService.archive(id);
    }
}