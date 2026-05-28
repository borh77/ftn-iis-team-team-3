package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    WHERE (:includeArchived = true OR v.status = :activeStatus)
      AND (:productId IS NULL OR p.id = :productId)
      AND (
            :search IS NULL
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(v.form) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(v.dosage) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
      )
    """)
    List<Variant> searchVariants(
        @Param("search") String search,
        @Param("productId") Long productId,
        @Param("includeArchived") boolean includeArchived,
        @Param("activeStatus") EntityStatus activeStatus
    );
}