package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicense;
import com.example.iisdrugcrm.domain.portfolio.MarketLicenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MarketLicenseRepository extends JpaRepository<MarketLicense, Long> {

    boolean existsByLicenseNumberIgnoreCase(String licenseNumber);

    boolean existsByLicenseNumberIgnoreCaseAndIdNot(String licenseNumber, Long id);

    boolean existsByMarketProductIdAndVariantVersionId(Long marketProductId, Long variantVersionId);

    @Query("""
        SELECT ml
        FROM MarketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
    """)
    List<MarketLicense> findAllWithRelations();

    @Query("""
        SELECT ml
        FROM MarketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE ml.status = :status
    """)
    List<MarketLicense> findByStatusWithRelations(MarketLicenseStatus status);

    @Query("""
        SELECT ml
        FROM MarketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE mp.id = :marketProductId
    """)
    List<MarketLicense> findByMarketProductIdWithRelations(Long marketProductId);

    @Query("""
        SELECT ml
        FROM MarketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE vv.id = :variantVersionId
    """)
    List<MarketLicense> findByVariantVersionIdWithRelations(Long variantVersionId);

    @Query("""
        SELECT ml
        FROM MarketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE LOWER(ml.licenseNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(mp.localName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(vv.versionLabel) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
    """)
    List<MarketLicense> searchByTextWithRelations(String search);

    @Query("""
        SELECT ml
        FROM MarketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE ml.validUntil <= :date
          AND ml.status IN ('APPROVED', 'RENEWAL_IN_PROGRESS')
    """)
    List<MarketLicense> findLicensesExpiringUntil(LocalDate date);
}