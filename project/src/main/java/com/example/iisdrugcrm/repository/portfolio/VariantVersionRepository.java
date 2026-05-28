package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersion;
import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VariantVersionRepository extends JpaRepository<VariantVersion, Long> {

    boolean existsByVariantIdAndVersionLabelIgnoreCase(Long variantId, String versionLabel);

    Optional<VariantVersion> findByVariantIdAndStatus(Long variantId, VariantVersionStatus status);

    @Query("""
        SELECT vv
        FROM VariantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
    """)
    List<VariantVersion> findAllWithRelations();

    @Query("""
        SELECT vv
        FROM VariantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE v.id = :variantId
    """)
    List<VariantVersion> findByVariantIdWithRelations(Long variantId);

    @Query("""
        SELECT vv
        FROM VariantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE vv.status = :status
    """)
    List<VariantVersion> findByStatusWithRelations(VariantVersionStatus status);

    @Query("""
        SELECT vv
        FROM VariantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE LOWER(vv.versionLabel) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.form) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(v.dosage) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    List<VariantVersion> searchByTextWithRelations(String search);
}