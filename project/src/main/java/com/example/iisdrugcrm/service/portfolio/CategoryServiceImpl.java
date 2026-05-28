package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.Category;
import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.dto.portfolio.CategoryRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.CategoryResponseDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponseDTO> getAllActive() {
        return categoryRepository.findByStatus(EntityStatus.ACTIVE)
                .stream()
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new PortfolioDuplicateResourceException("Category name already exists");
        }

        Category category = new Category(dto.getName(), dto.getDescription());
        return CategoryResponseDTO.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO dto) {
        Category category = getCategory(id);

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new PortfolioDuplicateResourceException("Category name already exists");
        }

        category.update(dto.getName(), dto.getDescription());
        return CategoryResponseDTO.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Category category = getCategory(id);
        category.archive();
        categoryRepository.save(category);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Category not found"));
    }
}