package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.SubcategoryCreateDTO;
import com.example.iisdrugcrm.dto.portfolio.SubcategoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.SubcategoryUpdateDTO;

import java.util.List;

public interface SubcategoryService {

    List<SubcategoryResponseDTO> getAllActive();

    List<SubcategoryResponseDTO> getActiveByCategory(Long categoryId);

    SubcategoryResponseDTO create(SubcategoryCreateDTO dto);

    SubcategoryResponseDTO update(Long id, SubcategoryUpdateDTO dto);

    void archive(Long id);
}