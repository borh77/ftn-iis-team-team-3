package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PricelistActivityLogRepository extends JpaRepository<PricelistActivityLog, Long>, JpaSpecificationExecutor<PricelistActivityLog> {

    @Query(value = """
            WITH active_transitions AS (
                SELECT pricelist_id, MIN(timestamp) AS active_at
                FROM pricelist_activity_logs
                WHERE action_type = 'STATUS_CHANGE'
                  AND status_to = 'ACTIVE'
                  AND timestamp >= :start
                  AND timestamp <= :end
                  AND (:teamId IS NULL OR team_id = :teamId)
                GROUP BY pricelist_id
            ),
            create_events AS (
                SELECT pricelist_id, MIN(timestamp) AS created_at
                FROM pricelist_activity_logs
                WHERE action_type = 'CREATE'
                GROUP BY pricelist_id
            ),
            review_events AS (
                SELECT pricelist_id, MIN(timestamp) AS review_started_at
                FROM pricelist_activity_logs
                WHERE action_type = 'STATUS_CHANGE'
                  AND status_to = 'IN_REVIEW'
                GROUP BY pricelist_id
            )
            SELECT
                COUNT(active.pricelist_id) AS "activatedPricelistsCount",
                COALESCE(AVG(EXTRACT(EPOCH FROM (active.active_at - created.created_at)) / 3600.0), 0) AS "averageTotalProcessingTimeHours",
                COALESCE(AVG(EXTRACT(EPOCH FROM (active.active_at - review.review_started_at)) / 3600.0), 0) AS "averageReviewTimeHours"
            FROM active_transitions active
            JOIN create_events created ON created.pricelist_id = active.pricelist_id
            LEFT JOIN review_events review ON review.pricelist_id = active.pricelist_id
            """, nativeQuery = true)
    PerformanceSummaryProjection findPerformanceSummary(
            @Param("teamId") Long teamId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query(value = """
            WITH active_transitions AS (
                SELECT pricelist_id, MIN(timestamp) AS active_at
                FROM pricelist_activity_logs
                WHERE action_type = 'STATUS_CHANGE'
                  AND status_to = 'ACTIVE'
                  AND timestamp >= :start
                  AND timestamp <= :end
                  AND (:teamId IS NULL OR team_id = :teamId)
                GROUP BY pricelist_id
            ),
            create_events AS (
                SELECT pricelist_id, MIN(timestamp) AS created_at
                FROM pricelist_activity_logs
                WHERE action_type = 'CREATE'
                GROUP BY pricelist_id
            )
            SELECT
                TO_CHAR(DATE_TRUNC('month', active.active_at AT TIME ZONE 'UTC'), 'YYYY-MM') AS "month",
                COALESCE(AVG(EXTRACT(EPOCH FROM (active.active_at - created.created_at)) / 3600.0), 0) AS "averageTotalProcessingTimeHours",
                COUNT(active.pricelist_id) AS "activatedPricelistsCount"
            FROM active_transitions active
            JOIN create_events created ON created.pricelist_id = active.pricelist_id
            GROUP BY "month"
            ORDER BY "month"
            """, nativeQuery = true)
    List<MonthlyPerformanceProjection> findMonthlyPerformanceTrend(
            @Param("teamId") Long teamId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    interface PerformanceSummaryProjection {
        Long getActivatedPricelistsCount();

        BigDecimal getAverageTotalProcessingTimeHours();

        BigDecimal getAverageReviewTimeHours();
    }

    interface MonthlyPerformanceProjection {
        String getMonth();

        BigDecimal getAverageTotalProcessingTimeHours();

        Long getActivatedPricelistsCount();
    }
}
