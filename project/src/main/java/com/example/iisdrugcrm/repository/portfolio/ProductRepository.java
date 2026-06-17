package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
          AND p.status = :status
    """)
    List<Product> searchByNameWithRelations(String search, EntityStatus status);

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
}