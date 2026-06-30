package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.dto.pricelist.MonthlyPerformancePointDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import com.example.iisdrugcrm.repository.PricelistRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricelistActivityLogServiceImpl implements PricelistActivityLogService {

    private final PricelistActivityLogRepository repository;
    private final PricelistRepository pricelistRepository;

    public PricelistActivityLogServiceImpl(PricelistActivityLogRepository repository, PricelistRepository pricelistRepository) {
        this.repository = repository;
        this.pricelistRepository = pricelistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PricelistActivityLogResponseDTO> findLogs(Long teamId, Long userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        Pageable effectivePageable = defaultSortIfUnsorted(pageable);
        return repository.findAll(filter(teamId, userId, from, to), effectivePageable)
                .map(PricelistActivityLogResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistActivityLogResponseDTO> findLogsForExport(Long teamId, Long userId, OffsetDateTime from, OffsetDateTime to) {
        return repository.findAll(filter(teamId, userId, from, to), defaultSort()).stream()
                .map(PricelistActivityLogResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamPerformanceReportDTO getPerformanceReport(Long teamId, OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end timestamps are required.");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start timestamp must be before or equal to end timestamp.");
        }

        OffsetDateTime normalizedStart = start.withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime normalizedEnd = end.withOffsetSameInstant(ZoneOffset.UTC);
        PricelistActivityLogRepository.PerformanceSummaryProjection summary =
                repository.findPerformanceSummary(teamId, normalizedStart, normalizedEnd);

        TeamPerformanceReportDTO report = new TeamPerformanceReportDTO();
        report.setTeamId(teamId);
        report.setPeriodStart(normalizedStart);
        report.setPeriodEnd(normalizedEnd);
        report.setActivatedPricelistsCount(nullSafe(summary.getActivatedPricelistsCount()));
        report.setAverageTotalProcessingTimeHours(nullSafe(summary.getAverageTotalProcessingTimeHours()));
        report.setAverageReviewTimeHours(nullSafe(summary.getAverageReviewTimeHours()));
        report.setStuckDraftCount(nullSafe(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.DRAFT, teamId)));
        report.setStuckInReviewCount(nullSafe(pricelistRepository.countByStatusAndOptionalTeamId(PricelistStatus.IN_REVIEW, teamId)));
        report.setMonthlyTrend(monthlyTrend(teamId, normalizedStart, normalizedEnd));
        return report;
    }

    private List<MonthlyPerformancePointDTO> monthlyTrend(Long teamId, OffsetDateTime start, OffsetDateTime end) {
        return repository.findMonthlyPerformanceTrend(teamId, start, end).stream()
                .map(point -> new MonthlyPerformancePointDTO(
                        point.getMonth(),
                        nullSafe(point.getAverageTotalProcessingTimeHours()),
                        nullSafe(point.getActivatedPricelistsCount())
                ))
                .toList();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long nullSafe(Long value) {
        return value == null ? 0L : value;
    }

    private Pageable defaultSortIfUnsorted(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 20, defaultSort());
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort());
    }

    private Sort defaultSort() {
        return Sort.by(Sort.Direction.DESC, "timestamp");
    }

    private Specification<PricelistActivityLog> filter(Long teamId, Long userId, OffsetDateTime from, OffsetDateTime to) {
        return Specification
                .where(teamEquals(teamId))
                .and(userEquals(userId))
                .and(timestampGreaterThanOrEqualTo(from))
                .and(timestampLessThanOrEqualTo(to));
    }

    private Specification<PricelistActivityLog> teamEquals(Long teamId) {
        return (root, query, criteriaBuilder) -> teamId == null ? null : criteriaBuilder.equal(root.get("teamId"), teamId);
    }

    private Specification<PricelistActivityLog> userEquals(Long userId) {
        return (root, query, criteriaBuilder) -> userId == null ? null : criteriaBuilder.equal(root.get("userId"), userId);
    }

    private Specification<PricelistActivityLog> timestampGreaterThanOrEqualTo(OffsetDateTime from) {
        return (root, query, criteriaBuilder) -> from == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from);
    }

    private Specification<PricelistActivityLog> timestampLessThanOrEqualTo(OffsetDateTime to) {
        return (root, query, criteriaBuilder) -> to == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to);
    }
}
