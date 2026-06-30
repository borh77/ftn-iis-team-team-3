package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
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
    void generateActivityLogPdfUsesNamesAndLocalTimestamps() throws IOException {
        PricelistActivityLogService activityLogService = mock(PricelistActivityLogService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PricelistTeamRepository teamRepository = mock(PricelistTeamRepository.class);
        ActivityLogPdfExportService pdfExportService = new ActivityLogPdfExportService(
                activityLogService,
                new PdfReportDisplayFormatter(userRepository, teamRepository)
        );
        OffsetDateTime from = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-06-30T23:59:59Z");

        when(activityLogService.findLogsForExport(eq(5L), eq(99L), eq(from), eq(to)))
                .thenReturn(List.of(log()));
        when(userRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any()))
                .thenReturn(List.of(user(99L, "marko", "marko@example.com", "Marko", "Markovic")));
        when(userRepository.findByUsername("admin"))
                .thenReturn(java.util.Optional.of(user(7L, "admin", "admin@example.com", "Ana", "Admin")));
        when(teamRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any()))
                .thenReturn(List.of(team(5L, "Commercial team")));

        byte[] pdf = pdfExportService.generateActivityLogPdf(5L, 99L, from, to, "admin");
        String text = pdfText(pdf);

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        assertTrue(text.contains("Marko Markovic <marko@example.com>"));
        assertTrue(text.contains("Ana Admin <admin@example.com>"));
        assertTrue(text.contains("Commercial team"));
        assertTrue(text.contains("27.06.2026 12:00"));
        assertTrue(text.contains("01.06.2026 02:00"));
        assertTrue(!text.contains("2026-06-27T10:00:00Z"));
        verify(activityLogService).findLogsForExport(eq(5L), eq(99L), eq(from), eq(to));
    }

    @Test
    void generateActivityLogPdfReturnsValidPdfForEmptyResultsAndMissingLookups() throws IOException {
        PricelistActivityLogService activityLogService = mock(PricelistActivityLogService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PricelistTeamRepository teamRepository = mock(PricelistTeamRepository.class);
        ActivityLogPdfExportService pdfExportService = new ActivityLogPdfExportService(
                activityLogService,
                new PdfReportDisplayFormatter(userRepository, teamRepository)
        );

        when(activityLogService.findLogsForExport(3L, 44L, null, null)).thenReturn(List.of());
        when(userRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any())).thenReturn(List.of());
        when(teamRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<Long>>any())).thenReturn(List.of());

        byte[] pdf = pdfExportService.generateActivityLogPdf(3L, 44L, null, null, "admin@example.com");
        String text = pdfText(pdf);

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, Math.min(pdf.length, 8), StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        assertTrue(text.contains("Team #3"));
        assertTrue(text.contains("User #44"));
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

    private User user(Long id, String username, String email, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
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
