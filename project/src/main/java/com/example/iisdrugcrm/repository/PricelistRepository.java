package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface PricelistRepository extends JpaRepository<Pricelist, Long> {

    List<Pricelist> findAllByOrderByIdDesc();

    List<Pricelist> findAllByCreatedByOrderByIdDesc(Long createdBy);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    @Query("""
            select p
            from Pricelist p
            where p.region.id = :regionId
              and lower(p.customerSegment) = lower(:customerSegment)
            """)
    List<Pricelist> lockByRegionAndCustomerSegment(
            @Param("regionId") Long regionId,
            @Param("customerSegment") String customerSegment
    );

    @Query("""
            select p
            from Pricelist p
            join fetch p.region r
            where r.id = :regionId
              and lower(p.customerSegment) = lower(:customerSegment)
              and p.status in :blockingStatuses
              and p.periodStart < :periodEnd
              and p.periodEnd > :periodStart
            order by p.periodStart asc
            """)
    List<Pricelist> findOverlappingBlockingPricelists(
            @Param("regionId") Long regionId,
            @Param("customerSegment") String customerSegment,
            @Param("periodStart") OffsetDateTime periodStart,
            @Param("periodEnd") OffsetDateTime periodEnd,
            @Param("blockingStatuses") List<PricelistStatus> blockingStatuses
    );

    @Query("""
            select p
            from Pricelist p
            join fetch p.region r
            where r.id = :regionId
              and lower(p.customerSegment) = lower(:customerSegment)
              and p.status in :blockingStatuses
              and p.id <> :excludedPricelistId
              and p.periodStart < :periodEnd
              and p.periodEnd > :periodStart
            order by p.periodStart asc
            """)
    List<Pricelist> findOverlappingBlockingPricelistsExcludingCurrent(
            @Param("regionId") Long regionId,
            @Param("customerSegment") String customerSegment,
            @Param("periodStart") OffsetDateTime periodStart,
            @Param("periodEnd") OffsetDateTime periodEnd,
            @Param("blockingStatuses") List<PricelistStatus> blockingStatuses,
            @Param("excludedPricelistId") Long excludedPricelistId
    );
}
