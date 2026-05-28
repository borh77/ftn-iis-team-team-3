package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionIngredientsRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionIngredientsResponseDTO;
import com.example.iisdrugcrm.service.portfolio.VariantVersionIngredientsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variant-version-ingredients")
public class VariantVersionIngredientsController {

    private final VariantVersionIngredientsService service;

    public VariantVersionIngredientsController(VariantVersionIngredientsService service) {
        this.service = service;
    }

    @GetMapping
    public List<VariantVersionIngredientsResponseDTO> getAll(
            @RequestParam(required = false) Long variantVersionId
    ) {
        return service.getAll(variantVersionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantVersionIngredientsResponseDTO create(
            @Valid @RequestBody VariantVersionIngredientsRequestDTO dto
    ) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantVersionIngredientsResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody VariantVersionIngredientsRequestDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}