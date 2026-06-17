package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.Ingredient;
import com.example.iisdrugcrm.domain.portfolio.VariantVersion;
import com.example.iisdrugcrm.domain.portfolio.VariantVersionIngredients;
import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionIngredientsRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionIngredientsResponseDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.IngredientRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionIngredientsRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VariantVersionIngredientsServiceImpl implements VariantVersionIngredientsService {

    private final VariantVersionIngredientsRepository repository;
    private final VariantVersionRepository variantVersionRepository;
    private final IngredientRepository ingredientRepository;

    public VariantVersionIngredientsServiceImpl(
            VariantVersionIngredientsRepository repository,
            VariantVersionRepository variantVersionRepository,
            IngredientRepository ingredientRepository
    ) {
        this.repository = repository;
        this.variantVersionRepository = variantVersionRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    public List<VariantVersionIngredientsResponseDTO> getAll(Long variantVersionId) {
        List<VariantVersionIngredients> items;

        if (variantVersionId != null) {
            items = repository.findByVariantVersionIdWithRelations(variantVersionId);
        } else {
            items = repository.findAllWithRelations();
        }

        return items.stream()
                .map(VariantVersionIngredientsResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public VariantVersionIngredientsResponseDTO create(VariantVersionIngredientsRequestDTO dto) {
        VariantVersion version = getVariantVersion(dto.getVariantVersionId());
        if (version.getStatus() != VariantVersionStatus.DEVELOPMENT) {
                throw new IllegalStateException("BOM can only be changed while version is in DEVELOPMENT");
        }
        
        Ingredient ingredient = getIngredient(dto.getIngredientId());

        if (repository.existsByVariantVersionIdAndIngredientId(
                dto.getVariantVersionId(),
                dto.getIngredientId()
        )) {
            throw new PortfolioDuplicateResourceException(
                    "Ingredient already exists in this variant version BOM"
            );
        }

        VariantVersionIngredients item = new VariantVersionIngredients(
                version,
                ingredient,
                dto.getAmount(),
                dto.getUnit()
        );

        return VariantVersionIngredientsResponseDTO.fromEntity(
                repository.save(item)
        );
    }

    @Override
    @Transactional
    public VariantVersionIngredientsResponseDTO update(
            Long id,
            VariantVersionIngredientsRequestDTO dto
    ) {
        VariantVersionIngredients item = getItem(id);

        VariantVersion version = getVariantVersion(dto.getVariantVersionId());

        if (version.getStatus() != VariantVersionStatus.DEVELOPMENT) {
                throw new IllegalStateException("BOM can only be changed while version is in DEVELOPMENT");
        }

        Ingredient ingredient = getIngredient(dto.getIngredientId());

        if (repository.existsByVariantVersionIdAndIngredientIdAndIdNot(
                dto.getVariantVersionId(),
                dto.getIngredientId(),
                id
        )) {
            throw new PortfolioDuplicateResourceException(
                    "Ingredient already exists in this variant version BOM"
            );
        }

        item.update(
                version,
                ingredient,
                dto.getAmount(),
                dto.getUnit()
        );

        return VariantVersionIngredientsResponseDTO.fromEntity(
                repository.save(item)
        );
    }

        @Override
        @Transactional
        public void delete(Long id) {
                VariantVersionIngredients item = getItem(id);

                VariantVersion version = item.getVariantVersion();
                if (version.getStatus() != VariantVersionStatus.DEVELOPMENT) {
                        throw new IllegalStateException("BOM can only be changed while version is in DEVELOPMENT");
                }

                repository.delete(item);
        }

    private VariantVersionIngredients getItem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("BOM item not found"));
    }

    private VariantVersion getVariantVersion(Long id) {
        return variantVersionRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Variant version not found"));
    }

    private Ingredient getIngredient(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Ingredient not found"));
    }
}