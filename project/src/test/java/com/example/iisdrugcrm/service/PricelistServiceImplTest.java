package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.*;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.pricelist.ChangePricelistStatusDTO;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.exception.InvalidPricelistThresholdException;
import com.example.iisdrugcrm.exception.InvalidPricelistStatusTransitionException;
import com.example.iisdrugcrm.exception.PricelistConflictException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.RegionRepository;
import com.example.iisdrugcrm.service.event.PricelistActionEvent;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PricelistServiceImplTest {

    @Mock
    private PricelistRepository pricelistRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private CatalogService catalogService;

    @Mock
    private PricelistAccessService accessService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PricelistServiceImpl service;
    private Region serbia;

    @BeforeEach
    void setUp() {
        service = new PricelistServiceImpl(pricelistRepository, regionRepository, catalogService, accessService, eventPublisher);
        serbia = region(1L, "Srbija", "RS");
        lenient().when(regionRepository.findById(1L)).thenReturn(Optional.of(serbia));
        lenient().when(catalogService.findActiveVariantsByIds(anyCollection()))
                .thenReturn(Map.of(10L, new CatalogVariantDTO(10L, "Variant A", true)));
        lenient().when(pricelistRepository.save(any(Pricelist.class))).thenAnswer(invocation -> {
            Pricelist pricelist = invocation.getArgument(0);
            if (pricelist.getId() == null) {
                pricelist.setId(1000L);
            }
            return pricelist;
        });
        lenient().when(accessService.accessibleCreatorIds(99L)).thenReturn(Set.of(99L));
        lenient().when(accessService.canCollaborate(any(Pricelist.class), eq(99L))).thenReturn(true);
    }

    @Test
    void conflictWithActivePricelistFails() {
        when(pricelistRepository.findOverlappingBlockingPricelists(eq(1L), eq("Lanci apoteka"), any(), any(), anyList()))
                .thenReturn(List.of(conflict(PricelistStatus.ACTIVE, serbia, "Lanci apoteka")));

        assertThrows(PricelistConflictException.class, () -> service.createCenovnik(validDto(), 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void conflictWithInReviewPricelistFails() {
        when(pricelistRepository.findOverlappingBlockingPricelists(eq(1L), eq("Lanci apoteka"), any(), any(), anyList()))
                .thenReturn(List.of(conflict(PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka")));

        assertThrows(PricelistConflictException.class, () -> service.createCenovnik(validDto(), 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void draftPricelistDoesNotBlockWhenRepositoryReturnsNoBlockingConflict() {
        noBlockingConflict();

        assertDoesNotThrow(() -> service.createCenovnik(validDto(), 99L));

        verify(pricelistRepository).save(any(Pricelist.class));
    }

    @Test
    void createPublishesActivityEvent() {
        noBlockingConflict();

        service.createCenovnik(validDto(), 99L);

        PricelistActionEvent event = capturedEvent();
        assertEquals(1000L, event.pricelistId());
        assertEquals(99L, event.userId());
        assertEquals(PricelistActionType.CREATE, event.actionType());
        assertEquals("Kreiran cenovnik u statusu DRAFT", event.description());
    }

    @Test
    void draftPricelistCanBeUpdated() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        CreatePricelistDTO dto = validDto();
        dto.setCustomerSegment("Bolnice");
        dto.getItems().get(0).getThresholds().get(1).setPrice(new BigDecimal("90.00"));

        service.update(100L, dto, 99L);

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        assertEquals("Bolnice", captor.getValue().getCustomerSegment());
        assertEquals(new BigDecimal("90.00"), captor.getValue().getItems().get(0).getThresholds().get(1).getPrice());
    }

    @Test
    void updatePublishesThresholdActivityEventWhenOnlyThresholdsChange() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();
        CreatePricelistDTO dto = validDto();
        dto.getItems().get(0).getThresholds().get(1).setPrice(new BigDecimal("90.00"));

        service.update(100L, dto, 99L);

        PricelistActionEvent event = capturedEvent();
        assertEquals(100L, event.pricelistId());
        assertEquals(99L, event.userId());
        assertEquals(PricelistActionType.UPDATE_THRESHOLDS, event.actionType());
        assertEquals("Azurirani pragovi cena cenovnika", event.description());
    }

    @Test
    void nonDraftPricelistCannotBeUpdated() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(IllegalArgumentException.class, () -> service.update(100L, validDto(), 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void archivedPricelistDoesNotBlockWhenRepositoryReturnsNoBlockingConflict() {
        noBlockingConflict();

        assertDoesNotThrow(() -> service.createCenovnik(validDto(), 99L));

        verify(pricelistRepository).save(any(Pricelist.class));
    }

    @Test
    void nonOverlappingPeriodSucceeds() {
        noBlockingConflict();
        CreatePricelistDTO dto = validDto();
        dto.setPeriodStart(OffsetDateTime.of(2026, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        dto.setPeriodEnd(OffsetDateTime.of(2026, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC));

        assertDoesNotThrow(() -> service.createCenovnik(dto, 99L));

        verify(pricelistRepository).save(any(Pricelist.class));
    }

    @Test
    void differentRegionSucceedsWhenNoBlockingConflictExists() {
        Region eu = region(2L, "EU", "EU");
        when(regionRepository.findById(2L)).thenReturn(Optional.of(eu));
        noBlockingConflict();
        CreatePricelistDTO dto = validDto();
        dto.setRegionId(2L);

        assertDoesNotThrow(() -> service.createCenovnik(dto, 99L));

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().getRegion().getId());
    }

    @Test
    void differentCustomerSegmentSucceedsWhenNoBlockingConflictExists() {
        noBlockingConflict();
        CreatePricelistDTO dto = validDto();
        dto.setCustomerSegment("Bolnice");

        assertDoesNotThrow(() -> service.createCenovnik(dto, 99L));

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        assertEquals("Bolnice", captor.getValue().getCustomerSegment());
    }

    @Test
    void ifThresholdValidationFailsNothingIsSaved() {
        CreatePricelistDTO dto = validDto();
        dto.getItems().get(0).getThresholds().get(1).setQuantityFrom(12);

        assertThrows(InvalidPricelistThresholdException.class, () -> service.createCenovnik(dto, 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void ifConflictExistsNothingIsSaved() {
        when(pricelistRepository.findOverlappingBlockingPricelists(eq(1L), eq("Lanci apoteka"), any(), any(), anyList()))
                .thenReturn(List.of(conflict(PricelistStatus.ACTIVE, serbia, "Lanci apoteka")));

        assertThrows(PricelistConflictException.class, () -> service.createCenovnik(validDto(), 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void overlapCheckUsesOnlyActiveAndInReviewStatuses() {
        noBlockingConflict();

        service.createCenovnik(validDto(), 99L);

        verify(pricelistRepository).findOverlappingBlockingPricelists(
                eq(1L),
                eq("Lanci apoteka"),
                any(),
                any(),
                eq(List.of(PricelistStatus.IN_REVIEW, PricelistStatus.ACTIVE))
        );
    }

    @Test
    void draftToInReviewSucceeds() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        service.changeStatus(100L, statusDto(PricelistStatus.IN_REVIEW, null));

        assertEquals(PricelistStatus.IN_REVIEW, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void unfinishedWizardDraftCannotMoveToInReview() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        pricelist.setCreationCompleted(false);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.IN_REVIEW, null)));

        assertEquals("Cenovnik nije kompletiran kroz wizard i ne moze biti poslat na proveru.", exception.getMessage());
        assertEquals(PricelistStatus.DRAFT, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void finishedWizardDraftCanMoveToInReview() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        pricelist.setCreationCompleted(true);
        pricelist.setCreationStep(PricelistCreationStep.COMPLETED);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        service.changeStatus(100L, statusDto(PricelistStatus.IN_REVIEW, null));

        assertEquals(PricelistStatus.IN_REVIEW, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void statusChangePublishesActivityEvent() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        service.changeStatus(100L, statusDto(PricelistStatus.IN_REVIEW, null), 99L);

        PricelistActionEvent event = capturedEvent();
        assertEquals(100L, event.pricelistId());
        assertEquals(99L, event.userId());
        assertEquals(PricelistActionType.STATUS_CHANGE, event.actionType());
        assertEquals("Promenjen status iz DRAFT u IN_REVIEW", event.description());
        assertEquals(PricelistStatus.DRAFT, event.statusFrom());
        assertEquals(PricelistStatus.IN_REVIEW, event.statusTo());
    }

    @Test
    void inReviewToActiveSucceedsWhenNoConflict() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null));

        assertEquals(PricelistStatus.ACTIVE, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void inReviewToDraftSucceedsWhenReasonIsPresent() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        service.changeStatus(100L, statusDto(PricelistStatus.DRAFT, "Needs correction"));

        assertEquals(PricelistStatus.DRAFT, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void inReviewToDraftFailsWhenReasonIsMissing() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(InvalidPricelistStatusTransitionException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.DRAFT, " ")));

        assertEquals(PricelistStatus.IN_REVIEW, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void activeToArchivedSucceeds() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        service.changeStatus(100L, statusDto(PricelistStatus.ARCHIVED, null));

        assertEquals(PricelistStatus.ARCHIVED, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void draftToActiveFails() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(InvalidPricelistStatusTransitionException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null)));

        assertEquals(PricelistStatus.DRAFT, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void activeToDraftFails() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(InvalidPricelistStatusTransitionException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.DRAFT, "No")));

        assertEquals(PricelistStatus.ACTIVE, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void archivedToDraftFails() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.ARCHIVED, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(InvalidPricelistStatusTransitionException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.DRAFT, "No")));

        assertEquals(PricelistStatus.ARCHIVED, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void archivedToActiveFails() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.ARCHIVED, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(InvalidPricelistStatusTransitionException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null)));

        assertEquals(PricelistStatus.ARCHIVED, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void activationFailsIfAnotherActivePricelistOverlaps() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(pricelistRepository.findOverlappingBlockingPricelistsExcludingCurrent(eq(1L), eq("Lanci apoteka"), any(), any(), anyList(), eq(100L)))
                .thenReturn(List.of(pricelist(101L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka")));

        assertThrows(PricelistConflictException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null)));

        assertEquals(PricelistStatus.IN_REVIEW, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void activationFailsIfAnotherInReviewPricelistOverlaps() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(pricelistRepository.findOverlappingBlockingPricelistsExcludingCurrent(eq(1L), eq("Lanci apoteka"), any(), any(), anyList(), eq(100L)))
                .thenReturn(List.of(pricelist(101L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka")));

        assertThrows(PricelistConflictException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null)));

        assertEquals(PricelistStatus.IN_REVIEW, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void activationSucceedsIfOverlapIsOnlyDraftOrArchived() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null));

        assertEquals(PricelistStatus.ACTIVE, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void activationSucceedsIfPeriodDoesNotOverlap() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null));

        assertEquals(PricelistStatus.ACTIVE, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void activationSucceedsForDifferentRegion() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, region(2L, "EU", "EU"), "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null));

        assertEquals(PricelistStatus.ACTIVE, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void activationSucceedsForDifferentCustomerSegment() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Bolnice");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null));

        assertEquals(PricelistStatus.ACTIVE, pricelist.getStatus());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void activationExcludesCurrentPricelistFromConflictCheck() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        noActivationConflict();

        service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null));

        verify(pricelistRepository).findOverlappingBlockingPricelistsExcludingCurrent(
                eq(1L),
                eq("Lanci apoteka"),
                any(),
                any(),
                eq(List.of(PricelistStatus.IN_REVIEW, PricelistStatus.ACTIVE)),
                eq(100L)
        );
    }

    @Test
    void teamListIncludesOwnAndTeammatePricelists() {
        Pricelist own = pricelist(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        Pricelist teammate = pricelist(101L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        teammate.setCreatedBy(7L);
        when(accessService.accessibleCreatorIds(99L)).thenReturn(Set.of(99L, 7L));
        when(accessService.canCollaborate(own, 99L)).thenReturn(true);
        when(accessService.canCollaborate(teammate, 99L)).thenReturn(true);
        when(pricelistRepository.findAllByCreatedByInOrderByIdDesc(Set.of(99L, 7L))).thenReturn(List.of(teammate, own));

        List<?> result = service.listTeamCenovniciForUser(99L);

        assertEquals(2, result.size());
        verify(pricelistRepository).findAllByCreatedByInOrderByIdDesc(Set.of(99L, 7L));
    }

    @Test
    void teammateCannotChangeStatusWhenOwnerOnlyRuleIsKept() {
        Pricelist pricelist = pricelist(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        doThrow(new IllegalArgumentException("Only the owner can change this pricelist status."))
                .when(accessService).validateOwnerOnly(pricelist, 7L);

        assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(100L, statusDto(PricelistStatus.IN_REVIEW, null), 7L));

        assertEquals(PricelistStatus.DRAFT, pricelist.getStatus());
        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void creatingNewVersionFromActiveSucceeds() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        source.setVersionNumber(1);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));
        when(pricelistRepository.findMaxVersionNumberForRoot(100L)).thenReturn(1);

        service.createNewVersion(100L, 99L);

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        Pricelist version = captor.getValue();
        assertEquals(PricelistStatus.DRAFT, version.getStatus());
        assertEquals(serbia, version.getRegion());
        assertEquals("Lanci apoteka", version.getCustomerSegment());
        assertEquals("RSD", version.getCurrency());
        assertEquals(source.getPeriodStart(), version.getPeriodStart());
        assertEquals(source.getPeriodEnd(), version.getPeriodEnd());
        assertEquals(100L, version.getParentPricelistId());
        assertEquals(100L, version.getRootPricelistId());
        assertEquals(2, version.getVersionNumber());
        assertEquals(PricelistStatus.ACTIVE, source.getStatus());
    }

    @Test
    void createNewVersionPublishesActivityEvent() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        source.setVersionNumber(1);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));
        when(pricelistRepository.findMaxVersionNumberForRoot(100L)).thenReturn(1);

        service.createNewVersion(100L, 99L);

        PricelistActionEvent event = capturedEvent();
        assertEquals(1000L, event.pricelistId());
        assertEquals(99L, event.userId());
        assertEquals(PricelistActionType.CREATE_VERSION, event.actionType());
        assertEquals("Kreirana nova verzija cenovnika", event.description());
    }

    @Test
    void teammateCanCreateNewVersionAndOwnsNewDraft() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        source.setCreatedBy(99L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));
        when(pricelistRepository.findMaxVersionNumberForRoot(100L)).thenReturn(1);

        service.createNewVersion(100L, 7L);

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        assertEquals(7L, captor.getValue().getCreatedBy());
        assertEquals(PricelistStatus.DRAFT, captor.getValue().getStatus());
    }

    @Test
    void creatingNewVersionFromInReviewSucceeds() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        source.setVersionNumber(3);
        source.setRootPricelistId(50L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));
        when(pricelistRepository.findMaxVersionNumberForRoot(50L)).thenReturn(3);

        service.createNewVersion(100L, 99L);

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        Pricelist version = captor.getValue();
        assertEquals(PricelistStatus.DRAFT, version.getStatus());
        assertEquals(100L, version.getParentPricelistId());
        assertEquals(50L, version.getRootPricelistId());
        assertEquals(4, version.getVersionNumber());
    }

    @Test
    void newVersionDeepCopiesItemsAndThresholds() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));
        when(pricelistRepository.findMaxVersionNumberForRoot(100L)).thenReturn(1);

        service.createNewVersion(100L, 99L);

        ArgumentCaptor<Pricelist> captor = ArgumentCaptor.forClass(Pricelist.class);
        verify(pricelistRepository).save(captor.capture());
        PricelistItem sourceItem = source.getItems().get(0);
        PricelistItem copiedItem = captor.getValue().getItems().get(0);
        assertNotSame(sourceItem, copiedItem);
        assertEquals(sourceItem.getVariantId(), copiedItem.getVariantId());
        assertEquals(sourceItem.getVariantName(), copiedItem.getVariantName());
        assertNotSame(sourceItem.getThresholds().get(0), copiedItem.getThresholds().get(0));
        assertEquals(sourceItem.getThresholds().get(0).getQuantityFrom(), copiedItem.getThresholds().get(0).getQuantityFrom());
        assertEquals(sourceItem.getThresholds().get(0).getQuantityTo(), copiedItem.getThresholds().get(0).getQuantityTo());
        assertEquals(sourceItem.getThresholds().get(0).getPrice(), copiedItem.getThresholds().get(0).getPrice());
    }

    @Test
    void archivedPricelistCannotBeVersioned() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.ARCHIVED, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));

        assertThrows(IllegalArgumentException.class, () -> service.createNewVersion(100L, 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void draftPricelistCannotBeVersioned() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));

        assertThrows(IllegalArgumentException.class, () -> service.createNewVersion(100L, 99L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void anotherCreatorCannotVersionPricelist() {
        Pricelist source = pricelistWithItem(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(source));
        doThrow(new IllegalArgumentException("You do not have access to this pricelist."))
                .when(accessService).validateOwnerOrTeamMember(source, 7L);

        assertThrows(IllegalArgumentException.class, () -> service.createNewVersion(100L, 7L));

        verify(pricelistRepository, never()).save(any(Pricelist.class));
    }

    @Test
    void ownerCanReplaceInactiveVariantOnDraftPricelistAndThresholdsArePreserved() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        PricelistItem item = pricelist.getItems().get(0);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(catalogService.findActiveVariantsByIds(List.of(11L)))
                .thenReturn(Map.of(11L, new CatalogVariantDTO(11L, "Variant B", true)));

        service.replaceItemVariant(100L, 500L, 11L, 99L);

        assertEquals(11L, item.getVariantId());
        assertEquals("Variant B", item.getVariantName());
        assertEquals(new BigDecimal("100.00"), item.getThresholds().get(0).getPrice());
        verify(pricelistRepository).save(pricelist);
    }

    @Test
    void replaceItemPublishesActivityEvent() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(catalogService.findActiveVariantsByIds(List.of(11L)))
                .thenReturn(Map.of(11L, new CatalogVariantDTO(11L, "Variant B", true)));

        service.replaceItemVariant(100L, 500L, 11L, 99L);

        PricelistActionEvent event = capturedEvent();
        assertEquals(100L, event.pricelistId());
        assertEquals(99L, event.userId());
        assertEquals(PricelistActionType.REPLACE_ITEM, event.actionType());
        assertEquals("Zamenjena stavka cenovnika", event.description());
    }

    @Test
    void cannotReplaceVariantOnActivePricelist() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.ACTIVE, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));

        assertThrows(IllegalArgumentException.class, () -> service.replaceItemVariant(100L, 500L, 11L, 99L));
    }

    @Test
    void replacementVariantMustBeActive() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(catalogService.findActiveVariantsByIds(List.of(11L))).thenReturn(Map.of());

        assertThrows(IllegalArgumentException.class, () -> service.replaceItemVariant(100L, 500L, 11L, 99L));
    }

    @Test
    void draftToInReviewFailsIfVariantIsInactive() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.DRAFT, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(catalogService.findActiveVariantsByIds(List.of(10L))).thenReturn(Map.of());

        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(100L, statusDto(PricelistStatus.IN_REVIEW, null)));
    }

    @Test
    void inReviewToActiveFailsIfVariantIsInactive() {
        Pricelist pricelist = pricelistWithItem(100L, PricelistStatus.IN_REVIEW, serbia, "Lanci apoteka");
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(pricelist));
        when(catalogService.findActiveVariantsByIds(List.of(10L))).thenReturn(Map.of());

        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(100L, statusDto(PricelistStatus.ACTIVE, null)));
    }

    private void noBlockingConflict() {
        when(pricelistRepository.findOverlappingBlockingPricelists(any(), any(), any(), any(), anyList()))
                .thenReturn(List.of());
    }

    private void noActivationConflict() {
        when(pricelistRepository.findOverlappingBlockingPricelistsExcludingCurrent(any(), any(), any(), any(), anyList(), any()))
                .thenReturn(List.of());
    }

    private PricelistActionEvent capturedEvent() {
        ArgumentCaptor<PricelistActionEvent> captor = ArgumentCaptor.forClass(PricelistActionEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private ChangePricelistStatusDTO statusDto(PricelistStatus targetStatus, String reason) {
        ChangePricelistStatusDTO dto = new ChangePricelistStatusDTO();
        dto.setTargetStatus(targetStatus);
        dto.setReason(reason);
        return dto;
    }

    private CreatePricelistDTO validDto() {
        CreatePricelistDTO dto = new CreatePricelistDTO();
        dto.setRegionId(1L);
        dto.setCustomerSegment("Lanci apoteka");
        dto.setCurrency("RSD");
        dto.setPeriodStart(OffsetDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        dto.setPeriodEnd(OffsetDateTime.of(2026, 9, 30, 0, 0, 0, 0, ZoneOffset.UTC));

        CreatePricelistDTO.PricelistItemDTO item = new CreatePricelistDTO.PricelistItemDTO();
        item.setVariantId(10L);
        item.setVariantName("Variant A");
        item.setThresholds(List.of(
                threshold(1, 10, "100.00"),
                threshold(11, 50, "95.00"),
                threshold(51, null, "90.00")
        ));
        dto.setItems(List.of(item));
        return dto;
    }

    private CreatePricelistDTO.QuantityThresholdDTO threshold(int quantityFrom, Integer quantityTo, String price) {
        CreatePricelistDTO.QuantityThresholdDTO threshold = new CreatePricelistDTO.QuantityThresholdDTO();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }

    private Pricelist conflict(PricelistStatus status, Region region, String customerSegment) {
        return pricelist(null, status, region, customerSegment);
    }

    private Pricelist pricelist(Long id, PricelistStatus status, Region region, String customerSegment) {
        Pricelist pricelist = new Pricelist();
        pricelist.setId(id);
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(customerSegment);
        pricelist.setCurrency("RSD");
        pricelist.setStatus(status);
        pricelist.setPeriodStart(OffsetDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        pricelist.setPeriodEnd(OffsetDateTime.of(2026, 9, 30, 0, 0, 0, 0, ZoneOffset.UTC));
        pricelist.setCreatedBy(99L);
        return pricelist;
    }

    private Pricelist pricelistWithItem(Long id, PricelistStatus status, Region region, String customerSegment) {
        Pricelist pricelist = pricelist(id, status, region, customerSegment);
        PricelistItem item = new PricelistItem();
        item.setId(500L);
        item.setVariantId(10L);
        item.setVariantName("Variant A");
        item.setThresholds(List.of(quantityThreshold(1, 10, "100.00"), quantityThreshold(11, null, "95.00")));
        pricelist.addItem(item);
        return pricelist;
    }

    private QuantityThreshold quantityThreshold(Integer quantityFrom, Integer quantityTo, String price) {
        QuantityThreshold threshold = new QuantityThreshold();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }

    private Region region(Long id, String name, String code) {
        Region region = new Region(name, code);
        region.setId(id);
        return region;
    }
}
