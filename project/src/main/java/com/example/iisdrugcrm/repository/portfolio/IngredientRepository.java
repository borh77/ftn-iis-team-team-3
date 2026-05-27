package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByCasIgnoreCase(String cas);

    boolean existsByCasIgnoreCaseAndIdNot(String cas, Long id);

    List<Ingredient> findByStatus(EntityStatus status);

    List<Ingredient> findByNameContainingIgnoreCaseAndStatus(String name, EntityStatus status);

    List<Ingredient> findByChemicalFormulaContainingIgnoreCaseAndStatus(String chemicalFormula, EntityStatus status);

    List<Ingredient> findByCasContainingIgnoreCaseAndStatus(String cas, EntityStatus status);
}