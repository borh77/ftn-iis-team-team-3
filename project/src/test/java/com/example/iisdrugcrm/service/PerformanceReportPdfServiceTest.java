package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.MonthlyPerformancePointDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PerformanceReportPdfServiceTest {

    @Test
    void generatePerformanceReportPdfReturnsNonEmptyPdfBytes() {
        PricelistActivityLogService activityLogService = mock(PricelistActivityLogService.class);
        PerformanceReportPdfService pdfService = new PerformanceReportPdfService(activityLogService);
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        when(activityLogService.getPerformanceReport(eq(5L), eq(start), eq(end))).thenReturn(report());

        byte[] pdf = pdfService.generatePerformanceReportPdf(5L, start, end);

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        verify(activityLogService).getPerformanceReport(eq(5L), eq(start), eq(end));
    }

    private TeamPerformanceReportDTO report() {
        TeamPerformanceReportDTO report = new TeamPerformanceReportDTO();
        report.setTeamId(5L);
        report.setPeriodStart(OffsetDateTime.parse("2026-06-01T00:00:00Z"));
        report.setPeriodEnd(OffsetDateTime.parse("2026-06-30T23:59:59Z"));
        report.setAverageTotalProcessingTimeHours(new BigDecimal("48.50"));
        report.setAverageReviewTimeHours(new BigDecimal("12.25"));
        report.setActivatedPricelistsCount(3L);
        report.setStuckDraftCount(2L);
        report.setStuckInReviewCount(1L);
        report.setMonthlyTrend(List.of(new MonthlyPerformancePointDTO("2026-06", new BigDecimal("48.50"), 3L)));
        report.setTeamFilterLimitation("Team filters use audit log team_id where available.");
        return report;
    }
}
