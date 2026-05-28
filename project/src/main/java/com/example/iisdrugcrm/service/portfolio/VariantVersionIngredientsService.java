package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionIngredientsRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionIngredientsResponseDTO;

import java.util.List;

public interface VariantVersionIngredientsService {

    List<VariantVersionIngredientsResponseDTO> getAll(Long variantVersionId);

    VariantVersionIngredientsResponseDTO create(VariantVersionIngredientsRequestDTO dto);

    VariantVersionIngredientsResponseDTO update(Long id, VariantVersionIngredientsRequestDTO dto);
}