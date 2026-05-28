package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Ingredient;
import com.example.iisdrugcrm.dto.portfolio.IngredientRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.IngredientResponseDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientServiceImpl(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    public List<IngredientResponseDTO> getAllActive(String search) {
        if (search == null || search.isBlank()) {
            return ingredientRepository.findByStatus(EntityStatus.ACTIVE)
                    .stream()
                    .map(IngredientResponseDTO::fromEntity)
                    .toList();
        }

        String term = search.trim();

        return ingredientRepository.findByNameContainingIgnoreCaseAndStatus(term, EntityStatus.ACTIVE)
                .stream()
                .map(IngredientResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public IngredientResponseDTO create(IngredientRequestDTO dto) {
        if (ingredientRepository.existsByCasIgnoreCase(dto.getCas())) {
            throw new PortfolioDuplicateResourceException("Ingredient CAS already exists");
        }

        Ingredient ingredient = new Ingredient(
                dto.getName(),
                dto.getChemicalFormula(),
                dto.getCas(),
                dto.getType()
        );

        return IngredientResponseDTO.fromEntity(ingredientRepository.save(ingredient));
    }

    @Override
    @Transactional
    public IngredientResponseDTO update(Long id, IngredientRequestDTO dto) {
        Ingredient ingredient = getIngredient(id);

        if (ingredientRepository.existsByCasIgnoreCaseAndIdNot(dto.getCas(), id)) {
            throw new PortfolioDuplicateResourceException("Ingredient CAS already exists");
        }

        ingredient.update(
                dto.getName(),
                dto.getChemicalFormula(),
                dto.getCas(),
                dto.getType()
        );

        return IngredientResponseDTO.fromEntity(ingredientRepository.save(ingredient));
    }

    @Override
    @Transactional
    public void archive(Long id) {
        Ingredient ingredient = getIngredient(id);
        ingredient.archive();
        ingredientRepository.save(ingredient);
    }

    private Ingredient getIngredient(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Ingredient not found"));
    }
}