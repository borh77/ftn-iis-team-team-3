package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import java.time.OffsetDateTime;
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

    public PricelistActivityLogServiceImpl(PricelistActivityLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PricelistActivityLogResponseDTO> findLogs(Long teamId, Long userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        Pageable effectivePageable = defaultSortIfUnsorted(pageable);
        return repository.findAll(filter(teamId, userId, from, to), effectivePageable)
                .map(PricelistActivityLogResponseDTO::fromEntity);
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
