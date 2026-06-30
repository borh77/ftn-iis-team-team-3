package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivityLogPdfExportServiceTest {

    @Test
    void generateActivityLogPdfReturnsNonEmptyPdfBytesAndUsesExportFilters() {
        PricelistActivityLogService activityLogService = mock(PricelistActivityLogService.class);
        ActivityLogPdfExportService pdfExportService = new ActivityLogPdfExportService(activityLogService);
        OffsetDateTime from = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        when(activityLogService.findLogsForExport(eq(5L), eq(99L), eq(from), eq(to)))
                .thenReturn(List.of(log()));

        byte[] pdf = pdfExportService.generateActivityLogPdf(5L, 99L, from, to, "admin@example.com");

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        verify(activityLogService).findLogsForExport(eq(5L), eq(99L), eq(from), eq(to));
    }

    @Test
    void generateActivityLogPdfReturnsValidPdfForEmptyResults() {
        PricelistActivityLogService activityLogService = mock(PricelistActivityLogService.class);
        ActivityLogPdfExportService pdfExportService = new ActivityLogPdfExportService(activityLogService);

        when(activityLogService.findLogsForExport(null, null, null, null)).thenReturn(List.of());

        byte[] pdf = pdfExportService.generateActivityLogPdf(null, null, null, null, "admin@example.com");

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    private PricelistActivityLogResponseDTO log() {
        PricelistActivityLogResponseDTO log = new PricelistActivityLogResponseDTO();
        log.setId(1L);
        log.setPricelistId(10L);
        log.setUserId(99L);
        log.setTeamId(5L);
        log.setActionType(PricelistActionType.STATUS_CHANGE);
        log.setDescription("Changed status from DRAFT to IN_REVIEW");
        log.setTimestamp(OffsetDateTime.parse("2026-06-27T10:00:00Z"));
        log.setStatusFrom(PricelistStatus.DRAFT);
        log.setStatusTo(PricelistStatus.IN_REVIEW);
        return log;
    }
}
