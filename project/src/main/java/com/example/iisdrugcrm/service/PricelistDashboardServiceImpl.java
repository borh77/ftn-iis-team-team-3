package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistDashboardSummaryDTO;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricelistDashboardServiceImpl implements PricelistDashboardService {

    private static final int RECENT_LIMIT = 5;

    private final PricelistRepository pricelistRepository;
    private final PricelistActivityLogRepository activityLogRepository;
    private final SpecialOfferRepository specialOfferRepository;
    private final PricelistAccessService accessService;

    public PricelistDashboardServiceImpl(
            PricelistRepository pricelistRepository,
            PricelistActivityLogRepository activityLogRepository,
            SpecialOfferRepository specialOfferRepository,
            PricelistAccessService accessService
    ) {
        this.pricelistRepository = pricelistRepository;
        this.activityLogRepository = activityLogRepository;
        this.specialOfferRepository = specialOfferRepository;
        this.accessService = accessService;
    }

    @Override
    @Transactional(readOnly = true)
    public PricelistDashboardSummaryDTO getSummary(
            Long currentUserId,
            boolean admin,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Long teamId,
            Long regionId,
            PricelistStatus status,
            String customerSegment
    ) {
        List<Pricelist> visiblePricelists = visiblePricelists(currentUserId, admin);
        List<Pricelist> filteredPricelists = visiblePricelists.stream()
                .filter(pricelist -> matchesFilters(pricelist, dateFrom, dateTo, teamId, regionId, status, customerSegment))
                .toList();

        PricelistDashboardSummaryDTO summary = new PricelistDashboardSummaryDTO();
        summary.setAppliedFilters(filters(dateFrom, dateTo, teamId, regionId, status, customerSegment));
        summary.setTotalPricelists(filteredPricelists.size());
        summary.setStatusCounts(statusCounts(filteredPricelists));
        summary.setDraftCount(countByStatus(filteredPricelists, PricelistStatus.DRAFT));
        summary.setInReviewCount(countByStatus(filteredPricelists, PricelistStatus.IN_REVIEW));
        summary.setActiveCount(countByStatus(filteredPricelists, PricelistStatus.ACTIVE));
        summary.setArchivedCount(countByStatus(filteredPricelists, PricelistStatus.ARCHIVED));
        summary.setStuckDraftCount(summary.getDraftCount());
        summary.setStuckInReviewCount(summary.getInReviewCount());
        summary.setIncompleteDraftCount(filteredPricelists.stream()
                .filter(pricelist -> pricelist.getStatus() == PricelistStatus.DRAFT)
                .filter(pricelist -> !pricelist.isCreationCompleted())
                .count());
        summary.setWaitingForReviewCount(filteredPricelists.stream()
                .filter(pricelist -> pricelist.getStatus() == PricelistStatus.IN_REVIEW)
                .filter(pricelist -> admin || accessService.canActivateAsReviewer(pricelist, currentUserId, false, true))
                .count());
        summary.setPricelistsByRegion(breakdown(filteredPricelists, this::regionKey));
        summary.setPricelistsBySegment(breakdown(filteredPricelists, this::segmentKey));
        summary.setPricelistsByTeam(breakdown(filteredPricelists, this::teamKey));
        summary.setRecentPricelists(recentPricelists(filteredPricelists));
        summary.setActiveOffersCount(activeOffersCount(filteredPricelists));

        List<Long> pricelistIds = filteredPricelists.stream().map(Pricelist::getId).filter(Objects::nonNull).toList();
        List<PricelistActivityLog> activityLogs = activityLogsFor(pricelistIds, dateFrom, dateTo, teamId);
        summary.setRecentActivity(activityLogs.stream()
                .limit(RECENT_LIMIT)
                .map(PricelistActivityLogResponseDTO::fromEntity)
                .toList());
        summary.setActivityCountByActionType(activityBreakdown(activityLogs));
        applyLifecycleMetrics(summary, lifecycleLogsFor(pricelistIds), dateFrom, dateTo);

        return summary;
    }

    private List<Pricelist> visiblePricelists(Long currentUserId, boolean admin) {
        if (admin) {
            return pricelistRepository.findAllByOrderByIdDesc();
        }
        Set<Long> accessibleCreatorIds = accessService.accessibleCreatorIds(currentUserId);
        return pricelistRepository.findAllByCreatedByInOrderByIdDesc(accessibleCreatorIds);
    }

    private boolean matchesFilters(
            Pricelist pricelist,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Long teamId,
            Long regionId,
            PricelistStatus status,
            String customerSegment
    ) {
        if (dateFrom != null && (pricelist.getPeriodEnd() == null || pricelist.getPeriodEnd().isBefore(dateFrom))) {
            return false;
        }
        if (dateTo != null && (pricelist.getPeriodStart() == null || pricelist.getPeriodStart().isAfter(dateTo))) {
            return false;
        }
        if (teamId != null && (pricelist.getTeam() == null || !teamId.equals(pricelist.getTeam().getId()))) {
            return false;
        }
        if (regionId != null && (pricelist.getRegion() == null || !regionId.equals(pricelist.getRegion().getId()))) {
            return false;
        }
        if (status != null && pricelist.getStatus() != status) {
            return false;
        }
        return customerSegment == null
                || customerSegment.isBlank()
                || customerSegment.equalsIgnoreCase(pricelist.getCustomerSegment());
    }

    private Map<PricelistStatus, Long> statusCounts(List<Pricelist> pricelists) {
        Map<PricelistStatus, Long> counts = new EnumMap<>(PricelistStatus.class);
        for (PricelistStatus status : PricelistStatus.values()) {
            counts.put(status, countByStatus(pricelists, status));
        }
        return counts;
    }

    private long countByStatus(List<Pricelist> pricelists, PricelistStatus status) {
        return pricelists.stream().filter(pricelist -> pricelist.getStatus() == status).count();
    }

    private List<PricelistDashboardSummaryDTO.BreakdownItemDTO> breakdown(
            List<Pricelist> pricelists,
            Function<Pricelist, BreakdownKey> keyExtractor
    ) {
        Map<BreakdownKey, Long> counts = pricelists.stream()
                .collect(Collectors.groupingBy(keyExtractor, LinkedHashMap::new, Collectors.counting()));

        return counts.entrySet().stream()
                .map(entry -> new PricelistDashboardSummaryDTO.BreakdownItemDTO(
                        entry.getKey().id(),
                        entry.getKey().label(),
                        entry.getValue()
                ))
                .sorted(Comparator
                        .comparingLong(PricelistDashboardSummaryDTO.BreakdownItemDTO::getCount).reversed()
                        .thenComparing(PricelistDashboardSummaryDTO.BreakdownItemDTO::getLabel))
                .limit(6)
                .toList();
    }

    private BreakdownKey regionKey(Pricelist pricelist) {
        if (pricelist.getRegion() == null) {
            return new BreakdownKey(null, "No region");
        }
        return new BreakdownKey(pricelist.getRegion().getId(), pricelist.getRegion().getName());
    }

    private BreakdownKey segmentKey(Pricelist pricelist) {
        String segment = pricelist.getCustomerSegment();
        return new BreakdownKey(null, segment == null || segment.isBlank() ? "No segment" : segment);
    }

    private BreakdownKey teamKey(Pricelist pricelist) {
        if (pricelist.getTeam() == null) {
            return new BreakdownKey(null, "Private");
        }
        return new BreakdownKey(pricelist.getTeam().getId(), pricelist.getTeam().getName());
    }

    private List<PricelistDashboardSummaryDTO.RecentPricelistDTO> recentPricelists(List<Pricelist> pricelists) {
        return pricelists.stream()
                .sorted(Comparator
                        .comparing(Pricelist::getLastEditedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Pricelist::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_LIMIT)
                .map(this::recentPricelist)
                .toList();
    }

    private PricelistDashboardSummaryDTO.RecentPricelistDTO recentPricelist(Pricelist pricelist) {
        PricelistDashboardSummaryDTO.RecentPricelistDTO dto = new PricelistDashboardSummaryDTO.RecentPricelistDTO();
        dto.setId(pricelist.getId());
        dto.setRegionName(pricelist.getRegion() == null ? null : pricelist.getRegion().getName());
        dto.setCustomerSegment(pricelist.getCustomerSegment());
        dto.setStatus(pricelist.getStatus());
        dto.setTeamName(pricelist.getTeam() == null ? "Private" : pricelist.getTeam().getName());
        dto.setItemCount(pricelist.getItems() == null ? 0 : pricelist.getItems().size());
        dto.setPeriodStart(pricelist.getPeriodStart());
        dto.setPeriodEnd(pricelist.getPeriodEnd());
        dto.setLastEditedAt(pricelist.getLastEditedAt());
        dto.setCreationCompleted(pricelist.isCreationCompleted());
        return dto;
    }

    private long activeOffersCount(List<Pricelist> pricelists) {
        Set<Long> ids = pricelists.stream().map(Pricelist::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        OffsetDateTime now = OffsetDateTime.now();
        return specialOfferRepository.findAll().stream()
                .filter(offer -> offer.getPricelist() != null && ids.contains(offer.getPricelist().getId()))
                .filter(offer -> offer.getStatus() == SpecialOfferStatus.ACTIVE)
                .filter(offer -> !offer.getStartDate().isAfter(now) && !offer.getEndDate().isBefore(now))
                .count();
    }

    private List<PricelistActivityLog> activityLogsFor(List<Long> pricelistIds, OffsetDateTime from, OffsetDateTime to, Long teamId) {
        if (pricelistIds.isEmpty()) {
            return List.of();
        }
        return activityLogRepository.findAll(activityFilter(pricelistIds, from, to, teamId), Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    private List<PricelistActivityLog> lifecycleLogsFor(List<Long> pricelistIds) {
        if (pricelistIds.isEmpty()) {
            return List.of();
        }
        return activityLogRepository.findAll(activityFilter(pricelistIds, null, null, null), Sort.by(Sort.Direction.ASC, "timestamp"));
    }

    private Specification<PricelistActivityLog> activityFilter(List<Long> pricelistIds, OffsetDateTime from, OffsetDateTime to, Long teamId) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("pricelistId").in(pricelistIds));
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            if (teamId != null) {
                predicates.add(criteriaBuilder.equal(root.get("teamId"), teamId));
            }
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private List<PricelistDashboardSummaryDTO.BreakdownItemDTO> activityBreakdown(List<PricelistActivityLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(PricelistActivityLog::getActionType, () -> new EnumMap<>(PricelistActionType.class), Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new PricelistDashboardSummaryDTO.BreakdownItemDTO(null, entry.getKey().name(), entry.getValue()))
                .sorted(Comparator
                        .comparingLong(PricelistDashboardSummaryDTO.BreakdownItemDTO::getCount).reversed()
                        .thenComparing(PricelistDashboardSummaryDTO.BreakdownItemDTO::getLabel))
                .toList();
    }

    private void applyLifecycleMetrics(
            PricelistDashboardSummaryDTO summary,
            List<PricelistActivityLog> logs,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo
    ) {
        Map<Long, List<PricelistActivityLog>> logsByPricelist = logs.stream()
                .collect(Collectors.groupingBy(PricelistActivityLog::getPricelistId));
        List<BigDecimal> processingHours = new ArrayList<>();
        List<BigDecimal> reviewHours = new ArrayList<>();
        long activatedCount = 0;

        for (List<PricelistActivityLog> pricelistLogs : logsByPricelist.values()) {
            OffsetDateTime createdAt = firstTimestamp(pricelistLogs, log -> log.getActionType() == PricelistActionType.CREATE);
            OffsetDateTime reviewStartedAt = firstTimestamp(pricelistLogs, log -> log.getStatusTo() == PricelistStatus.IN_REVIEW);
            OffsetDateTime activatedAt = firstTimestamp(pricelistLogs, log -> log.getStatusTo() == PricelistStatus.ACTIVE);
            if (activatedAt == null || !within(activatedAt, dateFrom, dateTo)) {
                continue;
            }
            activatedCount++;
            if (createdAt != null && !activatedAt.isBefore(createdAt)) {
                processingHours.add(hoursBetween(createdAt, activatedAt));
            }
            if (reviewStartedAt != null && !activatedAt.isBefore(reviewStartedAt)) {
                reviewHours.add(hoursBetween(reviewStartedAt, activatedAt));
            }
        }

        summary.setActivatedPricelistsCount(activatedCount);
        summary.setAverageProcessingTimeHours(average(processingHours));
        summary.setAverageReviewTimeHours(average(reviewHours));
    }

    private OffsetDateTime firstTimestamp(List<PricelistActivityLog> logs, java.util.function.Predicate<PricelistActivityLog> predicate) {
        return logs.stream()
                .filter(predicate)
                .map(PricelistActivityLog::getTimestamp)
                .filter(Objects::nonNull)
                .min(OffsetDateTime::compareTo)
                .orElse(null);
    }

    private boolean within(OffsetDateTime timestamp, OffsetDateTime from, OffsetDateTime to) {
        return (from == null || !timestamp.isBefore(from)) && (to == null || !timestamp.isAfter(to));
    }

    private BigDecimal hoursBetween(OffsetDateTime start, OffsetDateTime end) {
        return BigDecimal.valueOf(Duration.between(start, end).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private PricelistDashboardSummaryDTO.DashboardFiltersDTO filters(
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Long teamId,
            Long regionId,
            PricelistStatus status,
            String customerSegment
    ) {
        PricelistDashboardSummaryDTO.DashboardFiltersDTO filters = new PricelistDashboardSummaryDTO.DashboardFiltersDTO();
        filters.setDateFrom(dateFrom);
        filters.setDateTo(dateTo);
        filters.setTeamId(teamId);
        filters.setRegionId(regionId);
        filters.setStatus(status);
        filters.setCustomerSegment(customerSegment);
        return filters;
    }

    private record BreakdownKey(Long id, String label) {
    }
}
