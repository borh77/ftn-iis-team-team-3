package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.VariantRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantResponseDTO;

import java.util.List;

public interface VariantService {

    List<VariantResponseDTO> getVariants(String search, Long productId, boolean includeArchived);

    VariantResponseDTO create(VariantRequestDTO dto);

    VariantResponseDTO update(Long id, VariantRequestDTO dto);

    void archive(Long id);
}