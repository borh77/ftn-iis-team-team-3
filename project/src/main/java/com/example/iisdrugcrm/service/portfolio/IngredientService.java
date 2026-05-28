package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.IngredientRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.IngredientResponseDTO;

import java.util.List;

public interface IngredientService {

    List<IngredientResponseDTO> getAllActive(String search);

    IngredientResponseDTO create(IngredientRequestDTO dto);

    IngredientResponseDTO update(Long id, IngredientRequestDTO dto);

    void archive(Long id);
}