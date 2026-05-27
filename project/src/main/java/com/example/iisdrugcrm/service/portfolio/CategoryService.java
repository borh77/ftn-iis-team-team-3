package com.example.iisdrugcrm.service.portfolio;


import com.example.iisdrugcrm.dto.portfolio.CategoryCreateDTO;
import com.example.iisdrugcrm.dto.portfolio.CategoryResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.CategoryUpdateDTO;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDTO> getAllActive();

    CategoryResponseDTO create(CategoryCreateDTO dto);

    CategoryResponseDTO update(Long id, CategoryUpdateDTO dto);

    void archive(Long id);
}