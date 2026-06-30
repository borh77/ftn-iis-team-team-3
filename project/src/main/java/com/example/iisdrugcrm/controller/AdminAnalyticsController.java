package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import com.example.iisdrugcrm.service.PerformanceReportPdfService;
import com.example.iisdrugcrm.service.PricelistActivityLogService;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAnalyticsController {

    private final PricelistActivityLogService activityLogService;
    private final PerformanceReportPdfService performanceReportPdfService;

    public AdminAnalyticsController(
            PricelistActivityLogService activityLogService,
            PerformanceReportPdfService performanceReportPdfService
    ) {
        this.activityLogService = activityLogService;
        this.performanceReportPdfService = performanceReportPdfService;
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PricelistActivityLogResponseDTO>> getLogs(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(activityLogService.findLogs(teamId, userId, from, to, pageable));
    }

    @GetMapping("/analytics/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamPerformanceReportDTO> getPerformanceReport(
            @RequestParam(required = false) Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        return ResponseEntity.ok(activityLogService.getPerformanceReport(teamId, start, end));
    }

    @GetMapping("/analytics/performance/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getPerformanceReportPdf(
            @RequestParam(required = false) Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        byte[] pdf = performanceReportPdfService.generatePerformanceReportPdf(teamId, start, end);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("team-performance-report.pdf")
                        .build()
                        .toString())
                .body(pdf);
    }
}
