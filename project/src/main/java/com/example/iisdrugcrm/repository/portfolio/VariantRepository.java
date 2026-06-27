package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, Long> {

    boolean existsByProductIdAndFormIgnoreCaseAndDosageIgnoreCase(
            Long productId,
            String form,
            String dosage
    );

    boolean existsByProductIdAndFormIgnoreCaseAndDosageIgnoreCaseAndIdNot(
            Long productId,
            String form,
            String dosage,
            Long id
    );

    @Query("""
    SELECT v
    FROM Variant v
    JOIN FETCH v.product p
    LEFT JOIN FETCH v.replacementVariant rv
    LEFT JOIN FETCH rv.product rvp
    WHERE (:includeArchived = true OR v.status = :activeStatus)
      AND (:productId IS NULL OR p.id = :productId)
      AND (
            :search IS NULL
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(v.form) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(v.dosage) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
      )
    """)
    List<Variant> findByStatusWithRelations(
        EntityStatus activeStatus, 
        boolean includeArchived, 
        Long productId, 
        String search
    );

    @Query("""
        SELECT v
        FROM Variant v
        JOIN FETCH v.product p
        LEFT JOIN FETCH v.replacementVariant rv
        LEFT JOIN FETCH rv.product rvp
        WHERE v.id IN :ids
    """)
    List<Variant> findByIdInWithRelationsIncludingReplacement(@Param("ids") Collection<Long> ids);

    @Query("""
        SELECT v
        FROM Variant v
        JOIN FETCH v.product p
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.form) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.dosage) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    List<Variant> searchByTextWithRelations(String search);

    @Query("""
        SELECT v
        FROM Variant v
        JOIN FETCH v.product p
        WHERE p.id = :productId
          AND v.status = :status
    """)
    List<Variant> findByProductIdWithRelations(Long productId, EntityStatus status);

    @Query("""
        SELECT v
        FROM Variant v
        JOIN FETCH v.product p
        LEFT JOIN FETCH v.replacementVariant rv
        LEFT JOIN FETCH rv.product rvp
    """)
    List<Variant> findAllWithRelations();
}
