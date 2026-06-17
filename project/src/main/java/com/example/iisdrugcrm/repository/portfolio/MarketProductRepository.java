package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.MarketProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarketProductRepository extends JpaRepository<MarketProduct, Long> {

    boolean existsByVariantIdAndRegionId(Long variantId, Long regionId);

    boolean existsByVariantIdAndRegionIdAndIdNot(Long variantId, Long regionId, Long id);

    boolean existsByBarcodeIgnoreCase(String barcode);

    boolean existsByBarcodeIgnoreCaseAndIdNot(String barcode, Long id);

    @Query("""
        SELECT mp
        FROM MarketProduct mp
        JOIN FETCH mp.variant v
        JOIN FETCH v.product p
        JOIN FETCH mp.region r
        WHERE mp.status = :status
    """)
    List<MarketProduct> findByStatusWithRelations(EntityStatus status);

    @Query("""
        SELECT mp
        FROM MarketProduct mp
        JOIN FETCH mp.variant v
        JOIN FETCH v.product p
        JOIN FETCH mp.region r
    """)
    List<MarketProduct> findAllWithRelations();

    @Query("""
        SELECT mp
        FROM MarketProduct mp
        JOIN FETCH mp.variant v
        JOIN FETCH v.product p
        JOIN FETCH mp.region r
        WHERE v.id = :variantId
          AND mp.status = :status
    """)
    List<MarketProduct> findByVariantIdWithRelations(Long variantId, EntityStatus status);

    @Query("""
        SELECT mp
        FROM MarketProduct mp
        JOIN FETCH mp.variant v
        JOIN FETCH v.product p
        JOIN FETCH mp.region r
        WHERE r.id = :regionId
          AND mp.status = :status
    """)
    List<MarketProduct> findByRegionIdWithRelations(Long regionId, EntityStatus status);

    @Query("""
        SELECT mp
        FROM MarketProduct mp
        JOIN FETCH mp.variant v
        JOIN FETCH v.product p
        JOIN FETCH mp.region r
        WHERE LOWER(mp.localName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(mp.barcode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
           OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
    """)
    List<MarketProduct> searchByTextWithRelations(String search);
}