package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Product> findByStatus(EntityStatus status);

    List<Product> findByNameContainingIgnoreCaseAndStatus(String name, EntityStatus status);

    List<Product> findBySubcategoryIdAndStatus(Long subcategoryId, EntityStatus status);

    List<Product> findByTherapeuticAreaIdAndStatus(Long therapeuticAreaId, EntityStatus status);

    @Query("""
    SELECT p
    FROM Product p
    JOIN FETCH p.subcategory s
    JOIN FETCH p.therapeuticArea ta
    WHERE (:includeArchived = true OR p.status = :activeStatus)
      AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
      AND (:subcategoryId IS NULL OR s.id = :subcategoryId)
      AND (:therapeuticAreaId IS NULL OR ta.id = :therapeuticAreaId)
    """)
    List<Product> searchProducts(
        @Param("search") String search,
        @Param("subcategoryId") Long subcategoryId,
        @Param("therapeuticAreaId") Long therapeuticAreaId,
        @Param("includeArchived") boolean includeArchived,
        @Param("activeStatus") EntityStatus activeStatus
    );
}