package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Product;
import com.example.iisdrugcrm.domain.portfolio.Subcategory;
import com.example.iisdrugcrm.domain.portfolio.TherapeuticArea;
import com.example.iisdrugcrm.dto.portfolio.ProductRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.ProductResponseDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.ProductRepository;
import com.example.iisdrugcrm.repository.portfolio.SubcategoryRepository;
import com.example.iisdrugcrm.repository.portfolio.TherapeuticAreaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final TherapeuticAreaRepository therapeuticAreaRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            SubcategoryRepository subcategoryRepository,
            TherapeuticAreaRepository therapeuticAreaRepository
    ) {
        this.productRepository = productRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.therapeuticAreaRepository = therapeuticAreaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProducts(
        String search,
        Long subcategoryId,
        Long therapeuticAreaId,
        boolean includeArchived
    ) {
        String normalizedSearch = search == null || search.isBlank()
            ? null
            : search.trim();

            return productRepository.searchProducts(
                normalizedSearch,
                subcategoryId,
                therapeuticAreaId,
                includeArchived,
                EntityStatus.ACTIVE
        )
        .stream()
        .map(ProductResponseDTO::fromEntity)
        .toList();
    }

    @Override
    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto) {
        if (productRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new PortfolioDuplicateResourceException("Product name already exists");
        }

        Subcategory subcategory = getSubcategory(dto.getSubcategoryId());
        TherapeuticArea therapeuticArea = getTherapeuticArea(dto.getTherapeuticAreaId());

        Product product = new Product(
                dto.getName(),
                dto.getDescription(),
                subcategory,
                therapeuticArea
        );

        return ProductResponseDTO.fromEntity(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product product = getProduct(id);

        if (productRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new PortfolioDuplicateResourceException("Product name already exists");
        }

        Subcategory subcategory = getSubcategory(dto.getSubcategoryId());
        TherapeuticArea therapeuticArea = getTherapeuticArea(dto.getTherapeuticAreaId());

        product.update(
                dto.getName(),
                dto.getDescription(),
                subcategory,
                therapeuticArea
        );

        return ProductResponseDTO.fromEntity(productRepository.save(product));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Product product = getProduct(id);
        product.archive();
        productRepository.save(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Product not found"));
    }

    private Subcategory getSubcategory(Long id) {
        return subcategoryRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Subcategory not found"));
    }

    private TherapeuticArea getTherapeuticArea(Long id) {
        return therapeuticAreaRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Therapeutic area not found"));
    }
}