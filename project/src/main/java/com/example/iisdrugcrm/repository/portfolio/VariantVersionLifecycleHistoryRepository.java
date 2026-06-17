package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionLifecycleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VariantVersionLifecycleHistoryRepository
        extends JpaRepository<VariantVersionLifecycleHistory, Long> {

    @Query("""
        SELECT h
        FROM VariantVersionLifecycleHistory h
        JOIN FETCH h.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE vv.id = :variantVersionId
        ORDER BY h.changedAt DESC
    """)
    List<VariantVersionLifecycleHistory> findByVariantVersionIdWithRelations(Long variantVersionId);

    @Query("""
        SELECT h
        FROM VariantVersionLifecycleHistory h
        JOIN FETCH h.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE v.id = :variantId
        ORDER BY h.changedAt DESC
    """)
    List<VariantVersionLifecycleHistory> findByVariantIdWithRelations(Long variantId);
}