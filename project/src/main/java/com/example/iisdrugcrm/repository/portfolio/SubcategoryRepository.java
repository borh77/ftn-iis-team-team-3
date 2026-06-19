package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    boolean existsByCategoryIdAndNameIgnoreCase(Long categoryId, String name);

    boolean existsByCategoryIdAndNameIgnoreCaseAndIdNot(Long categoryId, String name, Long id);

    List<Subcategory> findByStatus(EntityStatus status);

    List<Subcategory> findByCategoryIdAndStatus(Long categoryId, EntityStatus status);

    @Query("""
    SELECT s
    FROM Subcategory s
    JOIN FETCH s.category c
    WHERE s.status = :status
    """)
    List<Subcategory> findByStatusWithCategory(EntityStatus status);

    @Query("""
    SELECT s
    FROM Subcategory s
    JOIN FETCH s.category c
    WHERE c.id = :categoryId
    AND s.status = :status
    """)
    List<Subcategory> findByCategoryIdAndStatusWithCategory(Long categoryId, EntityStatus status);

}
