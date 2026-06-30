package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.dto.pricelist.MonthlyPerformancePointDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistDashboardSummaryDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PerformanceReportPdfServiceTest {

    @Test
    void generatePerformanceReportPdfUsesTeamNamesAndLocalTimestamps() throws IOException {
        PricelistActivityLogService activityLogService = mock(PricelistActivityLogService.class);
        PricelistDashboardService dashboardService = mock(PricelistDashboardService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PricelistTeamRepository teamRepository = mock(PricelistTeamRepository.class);
        PerformanceReportPdfService pdfService = new PerformanceReportPdfService(
                activityLogService,
                dashboardService,
                new PdfReportDisplayFormatter(userRepository, teamRepository)
        );
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        when(activityLogService.getPerformanceReport(eq(5L), eq(start), eq(end))).thenReturn(report());
        when(dashboardService.getSummary(isNull(), eq(true), eq(start), eq(end), eq(5L), isNull(), isNull(), isNull()))
                .thenReturn(dashboard());
        when(teamRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any()))
                .thenReturn(List.of(team(5L, "Commercial team")));

        byte[] pdf = pdfService.generatePerformanceReportPdf(5L, start, end);
        String text = pdfText(pdf);

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        assertTrue(text.contains("Commercial team"));
        assertTrue(text.contains("01.06.2026 02:00"));
        assertTrue(text.contains("01.07.2026 01:59"));
        assertTrue(!text.contains("Team ID 5"));
        assertTrue(!text.contains("2026-06-01T00:00:00Z"));
        verify(activityLogService).getPerformanceReport(eq(5L), eq(start), eq(end));
        verify(dashboardService).getSummary(isNull(), eq(true), eq(start), eq(end), eq(5L), isNull(), isNull(), isNull());
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
        return report;
    }

    private PricelistDashboardSummaryDTO dashboard() {
        PricelistDashboardSummaryDTO dashboard = new PricelistDashboardSummaryDTO();
        dashboard.setTotalPricelists(8);
        dashboard.setDraftCount(2);
        dashboard.setInReviewCount(1);
        dashboard.setActiveCount(4);
        dashboard.setArchivedCount(1);
        dashboard.setActiveOffersCount(3);
        dashboard.setActivatedPricelistsCount(3);
        dashboard.setAverageReviewTimeHours(new BigDecimal("12.25"));
        dashboard.setPricelistsByTeam(List.of(new PricelistDashboardSummaryDTO.BreakdownItemDTO(5L, "Team ID 5", 4)));
        dashboard.setPricelistsByRegion(List.of(new PricelistDashboardSummaryDTO.BreakdownItemDTO(2L, "Belgrade", 3)));
        return dashboard;
    }

    private PricelistTeam team(Long id, String name) {
        PricelistTeam team = new PricelistTeam(name, 7L);
        team.setId(id);
        return team;
    }

    private String pdfText(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            StringBuilder text = new StringBuilder();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                text.append(extractor.getTextFromPage(page));
            }
            return text.toString();
        } finally {
            reader.close();
        }
    }
}
