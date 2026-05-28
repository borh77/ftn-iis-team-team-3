package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantResponseDTO;
import com.example.iisdrugcrm.service.portfolio.VariantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
public class VariantController {

    private final VariantService variantService;

    public VariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    @GetMapping
    public List<VariantResponseDTO> getVariants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        return variantService.getVariants(search, productId, includeArchived);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantResponseDTO create(@Valid @RequestBody VariantRequestDTO dto) {
        return variantService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody VariantRequestDTO dto
    ) {
        return variantService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public void archive(@PathVariable Long id) {
        variantService.archive(id);
    }
}