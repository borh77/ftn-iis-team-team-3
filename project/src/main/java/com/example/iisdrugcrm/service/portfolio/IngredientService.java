package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.IngredientCreateDTO;
import com.example.iisdrugcrm.dto.portfolio.IngredientResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.IngredientUpdateDTO;

import java.util.List;

public interface IngredientService {

    List<IngredientResponseDTO> getAllActive(String search);

    IngredientResponseDTO create(IngredientCreateDTO dto);

    IngredientResponseDTO update(Long id, IngredientUpdateDTO dto);

    void archive(Long id);
}