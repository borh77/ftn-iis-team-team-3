package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.dto.pricelist.PricelistDashboardSummaryDTO;
import com.example.iisdrugcrm.service.PricelistDashboardService;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PricelistDashboardControllerTest {

    private PricelistDashboardService dashboardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dashboardService = mock(PricelistDashboardService.class);
        mockMvc = standaloneSetup(new PricelistDashboardController(dashboardService)).build();
    }

    @Test
    void dashboardEndpointBindsFiltersForAdminSummary() throws Exception {
        OffsetDateTime start = OffsetDateTime.parse("2026-06-01T00:00:00Z");
        OffsetDateTime end = OffsetDateTime.parse("2026-06-30T23:59:59Z");
        PricelistDashboardSummaryDTO summary = new PricelistDashboardSummaryDTO();
        summary.setTotalPricelists(8);
        summary.setActiveCount(4);

        when(dashboardService.getSummary(
                isNull(),
                eq(true),
                eq(start),
                eq(end),
                eq(5L),
                eq(2L),
                eq(PricelistStatus.ACTIVE),
                eq("Retail")
        )).thenReturn(summary);

        mockMvc.perform(get("/api/admin/analytics/pricelist-dashboard")
                        .param("dateFrom", "2026-06-01T00:00:00Z")
                        .param("dateTo", "2026-06-30T23:59:59Z")
                        .param("teamId", "5")
                        .param("regionId", "2")
                        .param("status", "ACTIVE")
                        .param("customerSegment", "Retail"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalPricelists").value(8))
                .andExpect(jsonPath("$.activeCount").value(4));

        verify(dashboardService).getSummary(
                isNull(),
                eq(true),
                eq(start),
                eq(end),
                eq(5L),
                eq(2L),
                eq(PricelistStatus.ACTIVE),
                eq("Retail")
        );
    }

    @Test
    void dashboardEndpointRequiresAdminRole() throws Exception {
        Method method = PricelistDashboardController.class.getMethod(
                "pricelistDashboard",
                OffsetDateTime.class,
                OffsetDateTime.class,
                Long.class,
                Long.class,
                PricelistStatus.class,
                String.class
        );

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }
}
