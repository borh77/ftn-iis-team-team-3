package com.example.iisdrugcrm.controller.portfolio;

import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.TherapeuticAreaResponseDTO;
import com.example.iisdrugcrm.service.portfolio.TherapeuticAreaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/therapeutic-areas")
public class TherapeuticAreaController {

    private final TherapeuticAreaService therapeuticAreaService;

    public TherapeuticAreaController(TherapeuticAreaService therapeuticAreaService) {
        this.therapeuticAreaService = therapeuticAreaService;
    }

    @GetMapping
    public List<TherapeuticAreaResponseDTO> getAllActive() {
        return therapeuticAreaService.getAllActive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public TherapeuticAreaResponseDTO create(@Valid @RequestBody TherapeuticAreaRequestDTO dto) {
        return therapeuticAreaService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public TherapeuticAreaResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody TherapeuticAreaRequestDTO dto
    ) {
        return therapeuticAreaService.update(id, dto);
    }

    @PatchMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_PORTFOLIO_MANAGER')")
    public void archive(@PathVariable Long id) {
        therapeuticAreaService.archive(id);
    }
}