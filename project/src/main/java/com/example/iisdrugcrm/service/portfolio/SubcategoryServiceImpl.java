package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.Category;
import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Subcategory;
import com.example.iisdrugcrm.dto.portfolio.SubcategoryRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.SubcategoryResponseDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.CategoryRepository;
import com.example.iisdrugcrm.repository.portfolio.SubcategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubcategoryServiceImpl implements SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;

    public SubcategoryServiceImpl(
            SubcategoryRepository subcategoryRepository,
            CategoryRepository categoryRepository
    ) {
        this.subcategoryRepository = subcategoryRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<SubcategoryResponseDTO> getAllActive() {
        return subcategoryRepository.findByStatusWithCategory(EntityStatus.ACTIVE)
                .stream()
                .map(SubcategoryResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public List<SubcategoryResponseDTO> getActiveByCategory(Long categoryId) {
        return subcategoryRepository.findByCategoryIdAndStatusWithCategory(categoryId, EntityStatus.ACTIVE)                
                .stream()
                .map(SubcategoryResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public SubcategoryResponseDTO create(SubcategoryRequestDTO dto) {
        Category category = getCategory(dto.getCategoryId());

        if (subcategoryRepository.existsByCategoryIdAndNameIgnoreCase(dto.getCategoryId(), dto.getName())) {
            throw new PortfolioDuplicateResourceException("Subcategory already exists in this category");
        }

        Subcategory subcategory = new Subcategory(category, dto.getName(), dto.getDescription());
        return SubcategoryResponseDTO.fromEntity(subcategoryRepository.save(subcategory));
    }

    @Override
    @Transactional
    public SubcategoryResponseDTO update(Long id, SubcategoryRequestDTO dto) {
        Subcategory subcategory = getSubcategory(id);
        Category category = getCategory(dto.getCategoryId());

        if (subcategoryRepository.existsByCategoryIdAndNameIgnoreCaseAndIdNot(
                dto.getCategoryId(),
                dto.getName(),
                id
        )) {
            throw new PortfolioDuplicateResourceException("Subcategory already exists in this category");
        }

        subcategory.update(category, dto.getName(), dto.getDescription());
        return SubcategoryResponseDTO.fromEntity(subcategoryRepository.save(subcategory));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Subcategory subcategory = getSubcategory(id);
        subcategory.archive();
        subcategoryRepository.save(subcategory);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Category not found"));
    }

    private Subcategory getSubcategory(Long id) {
        return subcategoryRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Subcategory not found"));
    }
}