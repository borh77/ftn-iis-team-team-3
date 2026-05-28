package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.SubcategoryRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.SubcategoryResponseDTO;

import java.util.List;

public interface SubcategoryService {

    List<SubcategoryResponseDTO> getAllActive();

    List<SubcategoryResponseDTO> getActiveByCategory(Long categoryId);

    SubcategoryResponseDTO create(SubcategoryRequestDTO dto);

    SubcategoryResponseDTO update(Long id, SubcategoryRequestDTO dto);

    void archive(Long id);
}