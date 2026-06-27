package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.repository.PricelistActivityLogRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricelistActivityLogServiceImplTest {

    @Mock
    private PricelistActivityLogRepository repository;

    private PricelistActivityLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PricelistActivityLogServiceImpl(repository);
    }

    @Test
    void findLogsAppliesDefaultTimestampSortAndMapsDtos() {
        PricelistActivityLog log = new PricelistActivityLog(
                10L,
                99L,
                null,
                PricelistActionType.STATUS_CHANGE,
                "Promenjen status iz DRAFT u IN_REVIEW",
                OffsetDateTime.parse("2026-06-27T10:00:00Z")
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        Page<PricelistActivityLogResponseDTO> result = service.findLogs(
                null,
                99L,
                OffsetDateTime.parse("2026-06-01T00:00:00Z"),
                OffsetDateTime.parse("2026-06-30T23:59:59Z"),
                PageRequest.of(0, 20)
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order timestampSort = pageableCaptor.getValue().getSort().getOrderFor("timestamp");
        assertEquals(Sort.Direction.DESC, timestampSort.getDirection());
        assertEquals(10L, result.getContent().get(0).getPricelistId());
        assertEquals(99L, result.getContent().get(0).getUserId());
        assertEquals(PricelistActionType.STATUS_CHANGE, result.getContent().get(0).getActionType());
    }

    @Test
    void findLogsKeepsExplicitSort() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.findLogs(null, null, null, null, PageRequest.of(1, 10, Sort.by(Sort.Direction.ASC, "userId")));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), pageableCaptor.capture());
        Sort.Order userSort = pageableCaptor.getValue().getSort().getOrderFor("userId");
        assertEquals(Sort.Direction.ASC, userSort.getDirection());
    }
}
