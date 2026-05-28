package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.IngredientRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.IngredientResponseDTO;
import com.example.iisdrugcrm.service.portfolio.IngredientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public List<IngredientResponseDTO> getAllActive(
            @RequestParam(required = false) String search
    ) {
        return ingredientService.getAllActive(search);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public IngredientResponseDTO create(@Valid @RequestBody IngredientRequestDTO dto) {
        return ingredientService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public IngredientResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequestDTO dto
    ) {
        return ingredientService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PORTFOLIO_MANAGER')")
    public void archive(@PathVariable Long id) {
        ingredientService.archive(id);
    }
}