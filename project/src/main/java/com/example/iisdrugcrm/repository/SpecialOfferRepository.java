package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpecialOfferRepository extends JpaRepository<SpecialOffer, Long> {

    List<SpecialOffer> findAllByPricelistIdOrderByIdDesc(Long pricelistId);

    @Query("""
            select o
            from SpecialOffer o
            where o.pricelist.id = :pricelistId
              and o.status = com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus.ACTIVE
              and o.startDate <= :now
              and o.endDate >= :now
            order by o.id desc
            """)
    List<SpecialOffer> findActiveOffersForPricelist(@Param("pricelistId") Long pricelistId, @Param("now") OffsetDateTime now);

    @Query("""
            select o
            from SpecialOffer o
            where o.pricelist.id = :pricelistId
              and o.variantId = :variantId
              and o.status = com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus.ACTIVE
              and o.startDate <= :now
              and o.endDate >= :now
            order by o.id desc
            """)
    List<SpecialOffer> findActiveOffersForVariant(
            @Param("pricelistId") Long pricelistId,
            @Param("variantId") Long variantId,
            @Param("now") OffsetDateTime now
    );

    @Query("""
            select o
            from SpecialOffer o
            where lower(o.pricelist.customerSegment) = lower(:customerSegment)
              and o.status = com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus.ACTIVE
              and o.startDate <= :now
              and o.endDate >= :now
            order by o.id desc
            """)
    List<SpecialOffer> findActiveOffersForCustomerSegment(
            @Param("customerSegment") String customerSegment,
            @Param("now") OffsetDateTime now
    );

    @Query("""
            select o
            from SpecialOffer o
            where lower(o.pricelist.customerSegment) = lower(:customerSegment)
            order by o.createdAt desc, o.id desc
            """)
    List<SpecialOffer> findAllOffersForCustomerSegment(@Param("customerSegment") String customerSegment);
}
