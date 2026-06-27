package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
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

    @Autowired
    private RegionRepository regionRepository;

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

    @Test
    void countsStuckDraftAndInReviewByCurrentPricelistStatusAndOptionalAuditTeam() {
        Region region = regionRepository.save(new Region("Serbia", "RS"));
        Pricelist teamFiveDraft = pricelist(region, PricelistStatus.DRAFT, "Hospitals");
        Pricelist teamSixDraft = pricelist(region, PricelistStatus.DRAFT, "Pharmacies");
        Pricelist teamFiveReview = pricelist(region, PricelistStatus.IN_REVIEW, "Clinics");
        Pricelist active = pricelist(region, PricelistStatus.ACTIVE, "Distributors");
        pricelistRepository.save(teamFiveDraft);
        pricelistRepository.save(teamSixDraft);
        pricelistRepository.save(teamFiveReview);
        pricelistRepository.save(active);
        repository.save(log(teamFiveDraft.getId(), 5L));
        repository.save(log(teamSixDraft.getId(), 6L));
        repository.save(log(teamFiveReview.getId(), 5L));
        repository.save(log(active.getId(), 5L));

        assertThat(pricelistRepository.countByStatusAndOptionalAuditTeamId(PricelistStatus.DRAFT, null)).isEqualTo(2L);
        assertThat(pricelistRepository.countByStatusAndOptionalAuditTeamId(PricelistStatus.IN_REVIEW, null)).isEqualTo(1L);
        assertThat(pricelistRepository.countByStatusAndOptionalAuditTeamId(PricelistStatus.DRAFT, 5L)).isEqualTo(1L);
        assertThat(pricelistRepository.countByStatusAndOptionalAuditTeamId(PricelistStatus.IN_REVIEW, 5L)).isEqualTo(1L);
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

    private Pricelist pricelist(Region region, PricelistStatus status, String segment) {
        Pricelist pricelist = new Pricelist();
        pricelist.setRegion(region);
        pricelist.setStatus(status);
        pricelist.setCustomerSegment(segment);
        pricelist.setCurrency("EUR");
        pricelist.setPeriodStart(OffsetDateTime.parse("2026-06-01T00:00:00Z"));
        pricelist.setPeriodEnd(OffsetDateTime.parse("2026-06-30T23:59:59Z"));
        pricelist.setCreatedBy(99L);
        return pricelist;
    }

    private PricelistActivityLog log(Long pricelistId, Long teamId) {
        return new PricelistActivityLog(
                pricelistId,
                99L,
                teamId,
                PricelistActionType.CREATE,
                "Kreiran cenovnik u statusu DRAFT",
                OffsetDateTime.parse("2026-06-01T09:00:00Z")
        );
    }
}
