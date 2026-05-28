package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Product;
import com.example.iisdrugcrm.domain.portfolio.Variant;
import com.example.iisdrugcrm.dto.portfolio.VariantResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantRequestDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.ProductRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;

    public VariantServiceImpl(
            VariantRepository variantRepository,
            ProductRepository productRepository
    ) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<VariantResponseDTO> getVariants(String search, Long productId, boolean includeArchived) {
        String normalizedSearch = search == null || search.isBlank()
                ? null
                : search.trim();

        return variantRepository.searchVariants(
                        normalizedSearch,
                        productId,
                        includeArchived,
                        EntityStatus.ACTIVE
                )
                .stream()
                .map(VariantResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public VariantResponseDTO create(VariantRequestDTO dto) {
        Product product = getProduct(dto.getProductId());

        if (variantRepository.existsByProductIdAndFormIgnoreCaseAndDosageIgnoreCase(
                dto.getProductId(),
                dto.getForm(),
                dto.getDosage()
        )) {
            throw new PortfolioDuplicateResourceException("Variant already exists for this product");
        }

        Variant variant = new Variant(
                product,
                dto.getForm(),
                dto.getDosage()
        );

        return VariantResponseDTO.fromEntity(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public VariantResponseDTO update(Long id, VariantRequestDTO dto) {
        Variant variant = getVariant(id);
        Product product = getProduct(dto.getProductId());

        if (variantRepository.existsByProductIdAndFormIgnoreCaseAndDosageIgnoreCaseAndIdNot(
                dto.getProductId(),
                dto.getForm(),
                dto.getDosage(),
                id
        )) {
            throw new PortfolioDuplicateResourceException("Variant already exists for this product");
        }

        variant.update(
                product,
                dto.getForm(),
                dto.getDosage()
        );

        return VariantResponseDTO.fromEntity(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Variant variant = getVariant(id);
        variant.archive();
        variantRepository.save(variant);
    }

    private Variant getVariant(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Variant not found"));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Product not found"));
    }
}