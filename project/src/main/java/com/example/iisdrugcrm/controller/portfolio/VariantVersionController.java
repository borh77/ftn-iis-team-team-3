package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusRequestDTO;
import com.example.iisdrugcrm.service.portfolio.VariantVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/variant-versions")
public class VariantVersionController {

    private final VariantVersionService variantVersionService;

    public VariantVersionController(VariantVersionService variantVersionService) {
        this.variantVersionService = variantVersionService;
    }

    @GetMapping
    public List<VariantVersionResponseDTO> getVariantVersions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) VariantVersionStatus status
    ) {
        return variantVersionService.getVariantVersions(search, variantId, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantVersionResponseDTO create(
            @Valid @RequestBody VariantVersionRequestDTO dto
    ) {
        return variantVersionService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantVersionResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody VariantVersionRequestDTO dto
    ) {
        return variantVersionService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public VariantVersionResponseDTO changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody VariantVersionStatusRequestDTO dto
    ) {
        return variantVersionService.changeStatus(id, dto);
    }

    @GetMapping("/{id}/history")
    public List<VariantVersionLifecycleHistoryResponseDTO> getHistory(@PathVariable Long id) {
        return variantVersionService.getHistory(id);
    }
}