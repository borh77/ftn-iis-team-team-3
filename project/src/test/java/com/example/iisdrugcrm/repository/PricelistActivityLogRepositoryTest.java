package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.service.PricelistActivityLogServiceImpl;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PricelistActivityLogRepositoryTest {

    @Autowired
    private PricelistActivityLogRepository repository;

    @Autowired
    private PricelistRepository pricelistRepository;

    private PricelistActivityLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PricelistActivityLogServiceImpl(repository, pricelistRepository);
    }

    @Test
    void persistsAuditLogEntity() {
        PricelistActivityLog saved = repository.save(new PricelistActivityLog(
                10L,
                99L,
                5L,
                PricelistActionType.CREATE,
                "Kreiran cenovnik u statusu DRAFT",
                OffsetDateTime.parse("2026-06-27T10:00:00Z")
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .isPresent()
                .get()
                .satisfies(log -> {
                    assertThat(log.getPricelistId()).isEqualTo(10L);
                    assertThat(log.getUserId()).isEqualTo(99L);
                    assertThat(log.getTeamId()).isEqualTo(5L);
                    assertThat(log.getActionType()).isEqualTo(PricelistActionType.CREATE);
                    assertThat(log.getTimestamp()).isEqualTo(OffsetDateTime.parse("2026-06-27T10:00:00Z"));
                });
    }

    @Test
    void filtersLogsByUser() {
        seedLogs();

        Page<PricelistActivityLogResponseDTO> result = service.findLogs(
                null,
                99L,
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(PricelistActivityLogResponseDTO::getUserId)
                .containsExactly(99L, 99L);
    }

    @Test
    void filtersLogsByDateRange() {
        seedLogs();

        Page<PricelistActivityLogResponseDTO> result = service.findLogs(
                null,
                null,
                OffsetDateTime.parse("2026-06-10T00:00:00Z"),
                OffsetDateTime.parse("2026-06-20T23:59:59Z"),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent())
                .extracting(PricelistActivityLogResponseDTO::getTimestamp)
                .containsExactly(OffsetDateTime.parse("2026-06-15T12:00:00Z"));
    }

    @Test
    void paginatesNewestFirstByDefaultAndReturnsDtos() {
        seedLogs();

        Page<PricelistActivityLogResponseDTO> result = service.findLogs(
                null,
                null,
                null,
                null,
                PageRequest.of(0, 2)
        );

        assertThat(result.getContent())
                .hasSize(2)
                .allSatisfy(dto -> assertThat(dto).isNotInstanceOf(PricelistActivityLog.class))
                .extracting(PricelistActivityLogResponseDTO::getTimestamp)
                .containsExactly(
                        OffsetDateTime.parse("2026-06-25T08:30:00Z"),
                        OffsetDateTime.parse("2026-06-15T12:00:00Z")
                );
    }

    private void seedLogs() {
        repository.deleteAll();
        repository.save(new PricelistActivityLog(
                10L,
                99L,
                5L,
                PricelistActionType.CREATE,
                "Kreiran cenovnik u statusu DRAFT",
                OffsetDateTime.parse("2026-06-01T09:00:00Z")
        ));
        repository.save(new PricelistActivityLog(
                11L,
                42L,
                6L,
                PricelistActionType.UPDATE_ITEMS,
                "Izmenjene stavke cenovnika",
                OffsetDateTime.parse("2026-06-15T12:00:00Z")
        ));
        repository.save(new PricelistActivityLog(
                12L,
                99L,
                5L,
                PricelistActionType.STATUS_CHANGE,
                "Promenjen status iz DRAFT u IN_REVIEW",
                OffsetDateTime.parse("2026-06-25T08:30:00Z")
        ));
    }
}
