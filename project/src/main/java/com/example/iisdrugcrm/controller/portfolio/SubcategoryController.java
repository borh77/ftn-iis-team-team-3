package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.SubcategoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.SubcategoryRequestDTO;
import com.example.iisdrugcrm.service.portfolio.SubcategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcategories")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    public SubcategoryController(SubcategoryService subcategoryService) {
        this.subcategoryService = subcategoryService;
    }

    @GetMapping
    public List<SubcategoryResponseDTO> getAllActive(
            @RequestParam(required = false) Long categoryId
    ) {
        if (categoryId != null) {
            return subcategoryService.getActiveByCategory(categoryId);
        }

        return subcategoryService.getAllActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public SubcategoryResponseDTO create(@Valid @RequestBody SubcategoryRequestDTO dto) {
        return subcategoryService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public SubcategoryResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody SubcategoryRequestDTO dto
    ) {
        return subcategoryService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public void archive(@PathVariable Long id) {
        subcategoryService.archive(id);
    }
}