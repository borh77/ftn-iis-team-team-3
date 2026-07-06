package com.example.iisdrugcrm.mongo;

import com.example.iisdrugcrm.dto.adverse.AdverseEffectAnalyticsSummaryDTO;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SBP course integration: MongoDB aggregation pipelines, index management,
 * query plan analysis (explain) and data seeding for performance comparison.
 */
@Service
public class AdverseEffectMongoAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AdverseEffectMongoAnalyticsService.class);

    private static final String IDX_MED_STATUS_CREATED = "idx_med_status_created";
    private static final String IDX_EFFECT_LABELS = "idx_effect_labels";
    private static final String IDX_CREATED_AT = "idx_created_at";

    private static final int MAX_SEED_COUNT = 500_000;
    private static final int SEED_BATCH_SIZE = 5_000;

    private static final List<String> SEED_MEDICATIONS = List.of(
            "Brufen", "Paracetamol", "Aspirin", "Amoksicilin", "Andol",
            "Panklav", "Pressing", "Nurofen", "Eftil", "Bensedin",
            "Palitrex", "Diklofen");
    private static final List<String> SEED_EFFECTS = List.of(
            "Headache", "Nausea", "Dizziness", "Rash", "Fatigue",
            "Vomiting", "Insomnia", "Dry mouth", "Fever", "Stomach pain",
            "Palpitations", "Diarrhea", "Anxiety", "Blurred vision", "Muscle pain");
    private static final List<String> SEED_STATUSES = List.of(
            "SUBMITTED", "UNDER_REVIEW", "CLOSED", "EVIDENCED");
    private static final List<String> SEED_SEVERITIES = List.of(
            "MILD", "MODERATE", "SEVERE", "CRITICAL");
    private static final List<String> SEED_SOURCES = List.of(
            "Web", "Patient Portal", "Phone", "Email");

    private final MongoTemplate mongoTemplate;

    public AdverseEffectMongoAnalyticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private MongoCollection<Document> collection() {
        return mongoTemplate.getCollection(AdverseEffectReportDocument.COLLECTION);
    }

    // ------------------------------------------------------------------
    // Aggregation pipeline analytics ($match + $facet + $group + $unwind)
    // ------------------------------------------------------------------

    public AdverseEffectAnalyticsSummaryDTO getSummary(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<Document> pipeline = new ArrayList<>();
        Document createdAtFilter = createdAtRange(from, to);
        if (!createdAtFilter.isEmpty()) {
            pipeline.add(new Document("$match", new Document("createdAt", createdAtFilter)));
        }

        pipeline.add(new Document("$facet", new Document()
                .append("totals", List.of(new Document("$group", new Document("_id", null)
                        .append("total", new Document("$sum", 1))
                        .append("doctor", conditionalCount("$reportType", "DOCTOR"))
                        .append("patient", conditionalCount("$reportType", "PATIENT"))
                        .append("submitted", conditionalCount("$status", "SUBMITTED"))
                        .append("underReview", conditionalCount("$status", "UNDER_REVIEW"))
                        .append("closed", conditionalCount("$status", "CLOSED"))
                        .append("evidenced", conditionalCount("$status", "EVIDENCED")))))
                .append("byMedication", groupCountStage("$medicationName"))
                .append("byStatus", groupCountStage("$status"))
                .append("byReporterType", groupCountStage("$reporterType"))
                .append("bySource", groupCountStage("$sourceLabel"))
                .append("byEffect", List.of(
                        new Document("$unwind", "$effectLabels"),
                        new Document("$group", new Document("_id", "$effectLabels")
                                .append("count", new Document("$sum", 1))),
                        new Document("$sort", new Document("count", -1).append("_id", 1))))
                .append("overTime", List.of(
                        new Document("$group", new Document("_id", new Document("$dateToString",
                                new Document("format", "%Y-%m-%d").append("date", "$createdAt")))
                                .append("count", new Document("$sum", 1))),
                        new Document("$sort", new Document("_id", 1))))));

        Document result = collection().aggregate(pipeline).first();
        return toSummaryDTO(result);
    }

    private Document conditionalCount(String field, String value) {
        return new Document("$sum", new Document("$cond",
                List.of(new Document("$eq", List.of(field, value)), 1, 0)));
    }

    private List<Document> groupCountStage(String field) {
        return List.of(
                new Document("$group", new Document("_id", field)
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1).append("_id", 1)));
    }

    private AdverseEffectAnalyticsSummaryDTO toSummaryDTO(Document facetResult) {
        AdverseEffectAnalyticsSummaryDTO summary = new AdverseEffectAnalyticsSummaryDTO();
        if (facetResult == null) {
            return summary;
        }

        List<Document> totals = facetResult.getList("totals", Document.class, List.of());
        long total = 0;
        if (!totals.isEmpty()) {
            Document t = totals.get(0);
            total = longValue(t, "total");
            summary.setTotalReports(total);
            summary.setDoctorReports(longValue(t, "doctor"));
            summary.setPatientReports(longValue(t, "patient"));
            summary.setSubmittedReports(longValue(t, "submitted"));
            summary.setUnderReviewReports(longValue(t, "underReview"));
            summary.setClosedReports(longValue(t, "closed"));
            summary.setEvidencedReports(longValue(t, "evidenced"));
        }

        summary.setReportsByMedication(toCountItems(facetResult, "byMedication", total));
        summary.setReportsByEffect(toCountItems(facetResult, "byEffect", total));
        summary.setReportsByStatus(toCountItems(facetResult, "byStatus", total));
        summary.setReportsByReporterType(toCountItems(facetResult, "byReporterType", total));
        summary.setReportsBySource(toCountItems(facetResult, "bySource", total));

        List<AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO> buckets = new ArrayList<>();
        for (Document item : facetResult.getList("overTime", Document.class, List.of())) {
            buckets.add(new AdverseEffectAnalyticsSummaryDTO.TimeBucketDTO(
                    String.valueOf(item.get("_id")), longValue(item, "count")));
        }
        summary.setReportsOverTime(buckets);
        return summary;
    }

    private List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> toCountItems(Document facetResult, String facetName, long total) {
        List<AdverseEffectAnalyticsSummaryDTO.CountItemDTO> items = new ArrayList<>();
        for (Document item : facetResult.getList(facetName, Document.class, List.of())) {
            long count = longValue(item, "count");
            String label = item.get("_id") == null ? "Unknown" : String.valueOf(item.get("_id"));
            items.add(new AdverseEffectAnalyticsSummaryDTO.CountItemDTO(label, count, percentage(count, total)));
        }
        return items;
    }

    // ------------------------------------------------------------------
    // Query plan analysis (equivalent of Oracle EXPLAIN PLAN from class)
    // ------------------------------------------------------------------

    public Map<String, Object> explainQuery(String medicationName, String status, LocalDate from, LocalDate to) {
        Document filter = new Document();
        if (medicationName != null && !medicationName.isBlank()) {
            filter.append("medicationName", medicationName.trim());
        }
        if (status != null && !status.isBlank()) {
            filter.append("status", status.trim().toUpperCase());
        }
        Document createdAtFilter = createdAtRange(from, to);
        if (!createdAtFilter.isEmpty()) {
            filter.append("createdAt", createdAtFilter);
        }

        Document command = new Document("explain",
                new Document("find", AdverseEffectReportDocument.COLLECTION).append("filter", filter))
                .append("verbosity", "executionStats");
        Document explainResult = mongoTemplate.getDb().runCommand(command);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filter", filter.toJson());

        Document queryPlanner = (Document) explainResult.get("queryPlanner");
        if (queryPlanner != null) {
            Document winningPlan = (Document) queryPlanner.get("winningPlan");
            result.put("winningPlanStages", collectStages(winningPlan));
        }

        Document executionStats = (Document) explainResult.get("executionStats");
        if (executionStats != null) {
            result.put("nReturned", longValue(executionStats, "nReturned"));
            result.put("totalKeysExamined", longValue(executionStats, "totalKeysExamined"));
            result.put("totalDocsExamined", longValue(executionStats, "totalDocsExamined"));
            result.put("executionTimeMillis", longValue(executionStats, "executionTimeMillis"));
        }

        result.put("totalDocumentsInCollection", collection().estimatedDocumentCount());
        result.put("indexes", listIndexes());
        return result;
    }

    /** Walks winningPlan -> inputStage chain, e.g. ["FETCH", "IXSCAN (idx_med_status_created)"]. */
    private List<String> collectStages(Document plan) {
        List<String> stages = new ArrayList<>();
        Document current = plan;
        while (current != null) {
            // MongoDB 7 SBE plans wrap the classic plan inside "queryPlan"
            if (current.get("queryPlan") instanceof Document inner) {
                current = inner;
                continue;
            }
            String stage = current.getString("stage");
            if (stage != null) {
                String indexName = current.getString("indexName");
                stages.add(indexName == null ? stage : stage + " (" + indexName + ")");
            }
            current = current.get("inputStage") instanceof Document input ? input : null;
        }
        return stages;
    }

    // ------------------------------------------------------------------
    // Index management (create/drop on demand for before/after comparison)
    // ------------------------------------------------------------------

    public List<Map<String, Object>> createIndexes() {
        MongoCollection<Document> coll = collection();
        coll.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("medicationName", "status"),
                        Indexes.descending("createdAt")),
                new IndexOptions().name(IDX_MED_STATUS_CREATED));
        coll.createIndex(Indexes.ascending("effectLabels"), new IndexOptions().name(IDX_EFFECT_LABELS));
        coll.createIndex(Indexes.ascending("createdAt"), new IndexOptions().name(IDX_CREATED_AT));
        log.info("MongoDB analytics indexes created.");
        return listIndexes();
    }

    public List<Map<String, Object>> dropIndexes() {
        MongoCollection<Document> coll = collection();
        for (String indexName : List.of(IDX_MED_STATUS_CREATED, IDX_EFFECT_LABELS, IDX_CREATED_AT)) {
            try {
                coll.dropIndex(indexName);
            } catch (Exception exception) {
                log.debug("Index {} not dropped: {}", indexName, exception.getMessage());
            }
        }
        return listIndexes();
    }

    public List<Map<String, Object>> listIndexes() {
        List<Map<String, Object>> indexes = new ArrayList<>();
        for (Document index : collection().listIndexes()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", index.getString("name"));
            entry.put("keys", index.get("key", Document.class).toJson());
            indexes.add(entry);
        }
        return indexes;
    }

    // ------------------------------------------------------------------
    // Seeding for performance measurement (marked seeded=true, removable)
    // ------------------------------------------------------------------

    public Map<String, Object> seed(int count) {
        int target = Math.max(1, Math.min(count, MAX_SEED_COUNT));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        MongoCollection<Document> coll = collection();

        long inserted = 0;
        List<Document> batch = new ArrayList<>(SEED_BATCH_SIZE);
        for (int i = 0; i < target; i++) {
            batch.add(randomSeedDocument(random));
            if (batch.size() == SEED_BATCH_SIZE) {
                coll.insertMany(batch);
                inserted += batch.size();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            coll.insertMany(batch);
            inserted += batch.size();
        }

        log.info("Seeded {} synthetic adverse effect documents into MongoDB.", inserted);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inserted", inserted);
        result.put("totalDocumentsInCollection", coll.estimatedDocumentCount());
        return result;
    }

    public Map<String, Object> clearSeed() {
        long deleted = collection().deleteMany(new Document("seeded", true)).getDeletedCount();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deleted", deleted);
        result.put("totalDocumentsInCollection", collection().estimatedDocumentCount());
        return result;
    }

    private Document randomSeedDocument(ThreadLocalRandom random) {
        boolean doctor = random.nextInt(100) < 60;
        String status = doctor
                ? SEED_STATUSES.get(random.nextInt(3))
                : "EVIDENCED";
        Date createdAt = randomPastDate(random, 730);

        List<String> effects = new ArrayList<>();
        int effectCount = 1 + random.nextInt(3);
        while (effects.size() < effectCount) {
            String effect = SEED_EFFECTS.get(random.nextInt(SEED_EFFECTS.size()));
            if (!effects.contains(effect)) {
                effects.add(effect);
            }
        }

        String source = SEED_SOURCES.get(random.nextInt(SEED_SOURCES.size()));
        Document document = new Document("_id", -Math.abs(random.nextLong(1L, Long.MAX_VALUE)))
                .append("reportType", doctor ? "DOCTOR" : "PATIENT")
                .append("reporterType", doctor ? "Doctor" : "Patient")
                .append("status", status)
                .append("source", source)
                .append("sourceLabel", source)
                .append("severity", doctor ? SEED_SEVERITIES.get(random.nextInt(SEED_SEVERITIES.size())) : null)
                .append("medicationName", SEED_MEDICATIONS.get(random.nextInt(SEED_MEDICATIONS.size())))
                .append("effectLabels", effects)
                .append("createdAt", createdAt)
                .append("symptomDate", randomPastDate(random, 760))
                .append("reporterUsername", "seed_user_" + random.nextInt(50))
                .append("patientGender", random.nextBoolean() ? "Male" : "Female")
                .append("patientAge", 18 + random.nextInt(70))
                .append("statusHistory", List.of())
                .append("versions", List.of())
                .append("notes", List.of())
                .append("seeded", true)
                .append("syncedAt", new Date());
        return document;
    }

    private Date randomPastDate(ThreadLocalRandom random, int maxDaysBack) {
        LocalDateTime moment = LocalDateTime.now()
                .minusDays(random.nextInt(maxDaysBack))
                .minusMinutes(random.nextInt(24 * 60));
        return Date.from(moment.atZone(ZoneId.systemDefault()).toInstant());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Document createdAtRange(LocalDate from, LocalDate to) {
        Document range = new Document();
        if (from != null) {
            range.append("$gte", Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (to != null) {
            range.append("$lt", Date.from(to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        return range;
    }

    private long longValue(Document document, String key) {
        Object value = document.get(key);
        return value instanceof Number number ? number.longValue() : 0;
    }

    private double percentage(long count, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((count * 1000.0) / total) / 10.0;
    }
}
