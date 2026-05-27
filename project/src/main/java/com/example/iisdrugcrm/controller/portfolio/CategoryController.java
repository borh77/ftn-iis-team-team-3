package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.CategoryCreateDTO;
import com.example.iisdrugcrm.dto.portfolio.CategoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.CategoryUpdateDTO;
import com.example.iisdrugcrm.service.portfolio.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponseDTO> getAllActive() {
        return categoryService.getAllActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public CategoryResponseDTO create(@Valid @RequestBody CategoryCreateDTO dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public CategoryResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDTO dto
    ) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public void archive(@PathVariable Long id) {
        categoryService.archive(id);
    }
}