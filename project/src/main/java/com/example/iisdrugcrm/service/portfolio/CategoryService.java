package com.example.iisdrugcrm.service.portfolio;


import com.example.iisdrugcrm.dto.portfolio.CategoryRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDTO> getAllActive();

    CategoryResponseDTO create(CategoryRequestDTO dto);

    CategoryResponseDTO update(Long id, CategoryRequestDTO dto);

    void archive(Long id);
}