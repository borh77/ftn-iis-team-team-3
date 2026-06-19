package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
        SELECT p
        FROM Product p
        JOIN FETCH p.subcategory s
        JOIN FETCH p.therapeuticArea ta
        WHERE p.status = :status
    """)
    List<Product> findByStatusWithRelations(EntityStatus status);

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
    List<Product> searchByNameWithRelations(
        String search, 
        EntityStatus activeStatus, 
        boolean includeArchived, 
        Long subcategoryId, 
        Long therapeuticAreaId
    );

    @Query("""
        SELECT p
        FROM Product p
        JOIN FETCH p.subcategory s
        JOIN FETCH p.therapeuticArea ta
        WHERE s.id = :subcategoryId
          AND p.status = :status
    """)
    List<Product> findBySubcategoryIdWithRelations(Long subcategoryId, EntityStatus status);

    @Query("""
        SELECT p
        FROM Product p
        JOIN FETCH p.subcategory s
        JOIN FETCH p.therapeuticArea ta
        WHERE ta.id = :therapeuticAreaId
          AND p.status = :status
    """)
    List<Product> findByTherapeuticAreaIdWithRelations(Long therapeuticAreaId, EntityStatus status);

    @Query("""
        SELECT p
        FROM Product p
        JOIN FETCH p.subcategory s
        JOIN FETCH p.therapeuticArea ta
    """)
    List<Product> findAllWithRelations();

    @Query("""
        SELECT new com.example.iisdrugcrm.dto.portfolio.ProductCountByTherapeuticAreaDTO(
            ta.id,
            ta.name,
            COUNT(p)
        )
        FROM Product p
        JOIN p.therapeuticArea ta
        WHERE p.status = com.example.iisdrugcrm.domain.portfolio.EntityStatus.ACTIVE
        GROUP BY ta.id, ta.name
        ORDER BY COUNT(p) DESC
    """)
    List<ProductCountByTherapeuticAreaDTO> countActiveProductsByTherapeuticArea();

}