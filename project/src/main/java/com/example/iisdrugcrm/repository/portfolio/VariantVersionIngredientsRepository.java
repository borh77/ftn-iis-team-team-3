package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionIngredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VariantVersionIngredientsRepository extends JpaRepository<VariantVersionIngredients, Long> {

    boolean existsByVariantVersionIdAndIngredientId(Long variantVersionId, Long ingredientId);

    boolean existsByVariantVersionIdAndIngredientIdAndIdNot(
            Long variantVersionId,
            Long ingredientId,
            Long id
    );

    @Query("""
        SELECT vvi
        FROM VariantVersionIngredients vvi
        JOIN FETCH vvi.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        JOIN FETCH vvi.ingredient i
        WHERE vv.id = :variantVersionId
    """)
    List<VariantVersionIngredients> findByVariantVersionIdWithRelations(Long variantVersionId);

    @Query("""
        SELECT vvi
        FROM VariantVersionIngredients vvi
        JOIN FETCH vvi.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        JOIN FETCH vvi.ingredient i
    """)
    List<VariantVersionIngredients> findAllWithRelations();
}