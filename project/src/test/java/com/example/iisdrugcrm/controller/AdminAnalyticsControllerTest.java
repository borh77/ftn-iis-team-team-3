package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import com.example.iisdrugcrm.service.PerformanceReportPdfService;
import com.example.iisdrugcrm.service.PricelistActivityLogService;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AdminAnalyticsControllerTest {

    private PricelistActivityLogService service;
    private PerformanceReportPdfService pdfService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(PricelistActivityLogService.class);
        pdfService = mock(PerformanceReportPdfService.class);
        mockMvc = standaloneSetup(new AdminAnalyticsController(service, pdfService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void logsEndpointBindsFiltersAndDefaultsToNewestFirst() throws Exception {
        PricelistActivityLogResponseDTO dto = new PricelistActivityLogResponseDTO();
        dto.setId(1L);
        dto.setPricelistId(10L);
        dto.setUserId(99L);
        dto.setTeamId(5L);
        dto.setActionType(PricelistActionType.CREATE);
        dto.setDescription("Kreiran cenovnik u statusu DRAFT");
        dto.setTimestamp(OffsetDateTime.parse("2026-06-27T10:00:00Z"));
        when(service.findLogs(eq(5L), eq(99L), eq(OffsetDateTime.parse("2026-06-01T00:00:00Z")), eq(OffsetDateTime.parse("2026-06-30T23:59:59Z")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "timestamp")), 1));

        mockMvc.perform(get("/api/admin/logs")
                        .param("teamId", "5")
                        .param("userId", "99")
                        .param("from", "2026-06-01T00:00:00Z")
                        .param("to", "2026-06-30T23:59:59Z")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].actionType").value("CREATE"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findLogs(
                eq(5L),
                eq(99L),
                eq(OffsetDateTime.parse("2026-06-01T00:00:00Z")),
                eq(OffsetDateTime.parse("2026-06-30T23:59:59Z")),
                pageableCaptor.capture()
        );
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        Sort.Order timestampSort = pageable.getSort().getOrderFor("timestamp");
        assertEquals(Sort.Direction.DESC, timestampSort.getDirection());
    }

    @Test
    void logsEndpointRequiresAdminRole() throws Exception {
        Method method = AdminAnalyticsController.class.getMethod(
                "getLogs",
                Long.class,
                Long.class,
                OffsetDateTime.class,
                OffsetDateTime.class,
                Pageable.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    @Test
    void performanceEndpointBindsTeamAndPeriod() throws Exception {
        TeamPerformanceReportDTO dto = new TeamPerformanceReportDTO();
        dto.setTeamId(5L);
        dto.setAverageTotalProcessingTimeHours(new BigDecimal("48.50"));
        dto.setAverageReviewTimeHours(new BigDecimal("12.25"));
        dto.setActivatedPricelistsCount(1L);
        dto.setStuckDraftCount(2L);
        dto.setStuckInReviewCount(3L);
        when(service.getPerformanceReport(
                eq(5L),
                eq(OffsetDateTime.parse("2026-06-01T00:00:00Z")),
                eq(OffsetDateTime.parse("2026-06-30T23:59:59Z"))
        )).thenReturn(dto);

        mockMvc.perform(get("/api/admin/analytics/performance")
                        .param("teamId", "5")
                        .param("start", "2026-06-01T00:00:00Z")
                        .param("end", "2026-06-30T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.teamId").value(5))
                .andExpect(jsonPath("$.averageTotalProcessingTimeHours").value(48.50))
                .andExpect(jsonPath("$.averageReviewTimeHours").value(12.25))
                .andExpect(jsonPath("$.activatedPricelistsCount").value(1))
                .andExpect(jsonPath("$.stuckDraftCount").value(2))
                .andExpect(jsonPath("$.stuckInReviewCount").value(3));

        verify(service).getPerformanceReport(
                eq(5L),
                eq(OffsetDateTime.parse("2026-06-01T00:00:00Z")),
                eq(OffsetDateTime.parse("2026-06-30T23:59:59Z"))
        );
    }

    @Test
    void performanceEndpointRequiresAdminRole() throws Exception {
        Method method = AdminAnalyticsController.class.getMethod(
                "getPerformanceReport",
                Long.class,
                OffsetDateTime.class,
                OffsetDateTime.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    @Test
    void performancePdfEndpointReturnsAttachment() throws Exception {
        byte[] pdf = "%PDF-1.4 demo".getBytes();
        when(pdfService.generatePerformanceReportPdf(
                eq(5L),
                eq(OffsetDateTime.parse("2026-06-01T00:00:00Z")),
                eq(OffsetDateTime.parse("2026-06-30T23:59:59Z"))
        )).thenReturn(pdf);

        mockMvc.perform(get("/api/admin/analytics/performance/pdf")
                        .param("teamId", "5")
                        .param("start", "2026-06-01T00:00:00Z")
                        .param("end", "2026-06-30T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"team-performance-report.pdf\""));

        verify(pdfService).generatePerformanceReportPdf(
                eq(5L),
                eq(OffsetDateTime.parse("2026-06-01T00:00:00Z")),
                eq(OffsetDateTime.parse("2026-06-30T23:59:59Z"))
        );
    }

    @Test
    void performancePdfEndpointRequiresAdminRole() throws Exception {
        Method method = AdminAnalyticsController.class.getMethod(
                "getPerformanceReportPdf",
                Long.class,
                OffsetDateTime.class,
                OffsetDateTime.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }
}
