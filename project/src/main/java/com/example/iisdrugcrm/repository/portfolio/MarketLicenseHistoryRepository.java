package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.MarketLicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarketLicenseHistoryRepository extends JpaRepository<MarketLicenseHistory, Long> {

    @Query("""
        SELECT h
        FROM MarketLicenseHistory h
        JOIN FETCH h.marketLicense ml
        JOIN FETCH ml.marketProduct mp
        JOIN FETCH mp.region r
        JOIN FETCH mp.variant mv
        JOIN FETCH mv.product mvp
        JOIN FETCH ml.variantVersion vv
        JOIN FETCH vv.variant v
        JOIN FETCH v.product p
        WHERE ml.id = :marketLicenseId
        ORDER BY h.changedAt DESC
    """)
    List<MarketLicenseHistory> findByMarketLicenseIdWithRelations(Long marketLicenseId);
}