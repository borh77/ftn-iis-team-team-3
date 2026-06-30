package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import com.example.iisdrugcrm.repository.PricelistRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricelistActivityLogServiceImplTest {

    @Mock
    private PricelistActivityLogRepository repository;

    @Mock
    private PricelistRepository pricelistRepository;

    private PricelistActivityLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PricelistActivityLogServiceImpl(repository, pricelistRepository);
    }

    @Test
    void findLogsAppliesDefaultTimestampSortAndMapsDtos() {
        PricelistActivityLog log = new PricelistActivityLog(
                10L,
                99L,
                null,
                PricelistActionType.STATUS_CHANGE,
                "Changed status from DRAFT to IN_REVIEW",
                OffsetDateTime.parse("2026-06-27T10:00:00Z")
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        Page<PricelistActivityLogResponseDTO> result = service.findLogs(
                null,
                99L,
                OffsetDateTime.parse("2026-06-01T00:00:00Z"),
                OffsetDateTime.parse("2026-06-30T23:59:59Z"),
                PageRequest.of(0, 20)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order timestampSort = pageableCaptor.getValue().getSort().getOrderFor("timestamp");
        assertEquals(Sort.Direction.DESC, timestampSort.getDirection());
        assertEquals(10L, result.getContent().get(0).getPricelistId());
        assertEquals(99L, result.getContent().get(0).getUserId());
        assertEquals(PricelistActionType.STATUS_CHANGE, result.getContent().get(0).getActionType());
    }

    @Test
    void findLogsKeepsExplicitSort() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.findLogs(null, null, null, null, PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "userId")));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order userSort = pageableCaptor.getValue().getSort().getOrderFor("userId");
        assertEquals(Sort.Direction.ASC, userSort.getDirection());
    }

    @Test
    void findLogsForExportUsesSameFiltersWithoutPaging() {
        PricelistActivityLog log = new PricelistActivityLog(
                10L,
                99L,
                5L,
                PricelistActionType.CREATE,
                "Created pricelist",
                OffsetDateTime.parse("2026-06-27T10:00:00Z")
        );
        when(repository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(log));

        List<PricelistActivityLogResponseDTO> result = service.findLogsForExport(
                5L,
                99L,
                OffsetDateTime.parse("2026-06-01T00:00:00Z"),
                OffsetDateTime.parse("2026-06-30T23:59:59Z")
        );

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(repository).findAll(any(Specification.class), sortCaptor.capture());
        Sort.Order timestampSort = sortCaptor.getValue().getOrderFor("timestamp");
        assertEquals(Sort.Direction.DESC, timestampSort.getDirection());
        assertEquals(10L, result.get(0).getPricelistId());
        assertEquals(99L, result.get(0).getUserId());
        assertEquals(5L, result.get(0).getTeamId());
    }

    @Test
    void getPerformanceReportCalculatesTotalAndReviewTimeFromRepositoryProjection() {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");
        PerformanceSummaryStub summary = new PerformanceSummaryStub(
                1L,
                new BigDecimal("60.00"),
                new BigDecimal("36.00")
        );
        when(repository.findPerformanceSummary(null, start, end)).thenReturn(summary);
        when(repository.findMonthlyPerformanceTrend(null, start, end))
                .thenReturn(List.of(new MonthlyPerformanceStub("2026-06", new BigDecimal("60.00"), 1L)));
        when(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.DRAFT, null)).thenReturn(0L);
        when(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.IN_REVIEW, null)).thenReturn(0L);

        TeamPerformanceReportDTO report = service.getPerformanceReport(null, start, end);

        assertEquals(new BigDecimal("60.00"), report.getAverageTotalProcessingTimeHours());
        assertEquals(new BigDecimal("36.00"), report.getAverageReviewTimeHours());
        assertEquals(1L, report.getActivatedPricelistsCount());
        assertEquals(new BigDecimal("60.00"), report.getMonthlyTrend().get(0).getAverageTotalProcessingTimeHours());
    }

    @Test
    void getPerformanceReportMapsTeamFilterPeriodAndStuckCounts() {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00+02:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59+02:00");
        PerformanceSummaryStub summary = new PerformanceSummaryStub(
                1L,
                new BigDecimal("48.50"),
                new BigDecimal("12.25")
        );
        when(repository.findPerformanceSummary(eq(5L), eq(OffsetDateTime.parse("2026-05-31T22:00:00Z")), eq(OffsetDateTime.parse("2026-06-30T21:59:59Z"))))
                .thenReturn(summary);
        when(repository.findMonthlyPerformanceTrend(eq(5L), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(new MonthlyPerformanceStub("2026-06", new BigDecimal("48.50"), 1L)));
        when(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.DRAFT, 5L)).thenReturn(2L);
        when(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.IN_REVIEW, 5L)).thenReturn(3L);

        TeamPerformanceReportDTO report = service.getPerformanceReport(5L, start, end);

        assertEquals(5L, report.getTeamId());
        assertEquals(OffsetDateTime.parse("2026-05-31T22:00:00Z"), report.getPeriodStart());
        assertEquals(new BigDecimal("48.50"), report.getAverageTotalProcessingTimeHours());
        assertEquals(new BigDecimal("12.25"), report.getAverageReviewTimeHours());
        assertEquals(1L, report.getActivatedPricelistsCount());
        assertEquals(2L, report.getStuckDraftCount());
        assertEquals(3L, report.getStuckInReviewCount());
        assertEquals("2026-06", report.getMonthlyTrend().get(0).getMonth());
        assertEquals(new BigDecimal("48.50"), report.getMonthlyTrend().get(0).getAverageTotalProcessingTimeHours());
        verify(repository).findPerformanceSummary(5L, OffsetDateTime.parse("2026-05-31T22:00:00Z"), OffsetDateTime.parse("2026-06-30T21:59:59Z"));
        verify(pricelistRepository).countByStatusAndOptionalTeamId(PricelistStatus.DRAFT, 5L);
        verify(pricelistRepository).countByStatusAndOptionalTeamId(PricelistStatus.IN_REVIEW, 5L);
    }

    @Test
    void getPerformanceReportForAllTeamsPassesNullTeamFilterAndSelectedActivationPeriod() {
        OffsetDateTime start = OffsetDateTime.parse("2026-07-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-07-31T23:59:59Z");
        when(repository.findPerformanceSummary(null, start, end))
                .thenReturn(new PerformanceSummaryStub(0L, BigDecimal.ZERO, BigDecimal.ZERO));
        when(repository.findMonthlyPerformanceTrend(null, start, end)).thenReturn(List.of());
        when(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.DRAFT, null)).thenReturn(4L);
        when(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.IN_REVIEW, null)).thenReturn(2L);

        TeamPerformanceReportDTO report = service.getPerformanceReport(null, start, end);

        assertEquals(null, report.getTeamId());
        assertEquals(4L, report.getStuckDraftCount());
        assertEquals(2L, report.getStuckInReviewCount());
        assertEquals(null, report.getTeamFilterLimitation());
        verify(repository).findPerformanceSummary(null, start, end);
        verify(repository).findMonthlyPerformanceTrend(null, start, end);
    }

    private record PerformanceSummaryStub(
            Long activatedPricelistsCount,
            BigDecimal averageTotalProcessingTimeHours,
            BigDecimal averageReviewTimeHours
    ) implements PricelistActivityLogRepository.PerformanceSummaryProjection {
        @Override
        public Long getActivatedPricelistsCount() {
            return activatedPricelistsCount;
        }

        @Override
        public BigDecimal getAverageTotalProcessingTimeHours() {
            return averageTotalProcessingTimeHours;
        }

        @Override
        public BigDecimal getAverageReviewTimeHours() {
            return averageReviewTimeHours;
        }
    }

    private record MonthlyPerformanceStub(
            String month,
            BigDecimal averageTotalProcessingTimeHours,
            Long activatedPricelistsCount
    ) implements PricelistActivityLogRepository.MonthlyPerformanceProjection {
        @Override
        public String getMonth() {
            return month;
        }

        @Override
        public BigDecimal getAverageTotalProcessingTimeHours() {
            return averageTotalProcessingTimeHours;
        }

        @Override
        public Long getActivatedPricelistsCount() {
            return activatedPricelistsCount;
        }
    }
}
