package com.example.iisdrugcrm.mongo;

import com.example.iisdrugcrm.dto.adverse.AdverseEffectAnalyticsSummaryDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * SBP course integration endpoints: MongoDB analytics (aggregation pipelines),
 * data sync/seed and index management with query-plan analysis.
 */
@RestController
@RequestMapping("/api/adverse-effects/mongo")
@PreAuthorize("hasRole('FARMAKOVIGILANT')")
public class AdverseEffectMongoController {

    private final AdverseEffectMongoSyncService syncService;
    private final AdverseEffectMongoAnalyticsService analyticsService;

    public AdverseEffectMongoController(
            AdverseEffectMongoSyncService syncService,
            AdverseEffectMongoAnalyticsService analyticsService) {
        this.syncService = syncService;
        this.analyticsService = analyticsService;
    }

    /** Analytics summary computed by a single $facet aggregation pipeline in MongoDB. */
    @GetMapping("/analytics/summary")
    public ResponseEntity<AdverseEffectAnalyticsSummaryDTO> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getSummary(from, to));
    }

    /** Backfills all reports from PostgreSQL into MongoDB. */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncAll() {
        long synced = syncService.syncAll();
        return ResponseEntity.ok(Map.of("synced", synced));
    }

    /** Query plan analysis - MongoDB equivalent of Oracle EXPLAIN PLAN from class. */
    @GetMapping("/analytics/explain")
    public ResponseEntity<Map<String, Object>> explain(
            @RequestParam(required = false) String medicationName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.explainQuery(medicationName, status, from, to));
    }

    @GetMapping("/indexes")
    public ResponseEntity<List<Map<String, Object>>> listIndexes() {
        return ResponseEntity.ok(analyticsService.listIndexes());
    }

    @PostMapping("/indexes")
    public ResponseEntity<List<Map<String, Object>>> createIndexes() {
        return ResponseEntity.ok(analyticsService.createIndexes());
    }

    @DeleteMapping("/indexes")
    public ResponseEntity<List<Map<String, Object>>> dropIndexes() {
        return ResponseEntity.ok(analyticsService.dropIndexes());
    }

    /** Inserts synthetic documents (seeded=true) for performance comparison. */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed(@RequestParam(defaultValue = "200000") int count) {
        return ResponseEntity.ok(analyticsService.seed(count));
    }

    /** Removes all synthetic documents. */
    @DeleteMapping("/seed")
    public ResponseEntity<Map<String, Object>> clearSeed() {
        return ResponseEntity.ok(analyticsService.clearSeed());
    }
}
