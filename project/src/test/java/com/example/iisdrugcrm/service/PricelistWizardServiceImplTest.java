package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistCreationStep;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveBasicInfoStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveItemsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveThresholdsStepDTO;
import com.example.iisdrugcrm.exception.InvalidPricelistThresholdException;
import com.example.iisdrugcrm.exception.PricelistStartDateInPastException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.RegionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricelistWizardServiceImplTest {

    @Mock
    private PricelistRepository pricelistRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private PricelistTeamRepository teamRepository;

    @Mock
    private CatalogService catalogService;

    @Mock
    private PricelistAccessService accessService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PricelistWizardServiceImpl service;
    private Region serbia;

    @BeforeEach
    void setUp() {
        service = new PricelistWizardServiceImpl(
                pricelistRepository,
                regionRepository,
                teamRepository,
                catalogService,
                accessService,
                eventPublisher
        );
        serbia = region(1L, "Srbija", "RS");
        lenient().when(regionRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(serbia));
        lenient().when(regionRepository.findById(1L)).thenReturn(Optional.of(serbia));
        lenient().when(pricelistRepository.save(any(Pricelist.class))).thenAnswer(invocation -> {
            Pricelist pricelist = invocation.getArgument(0);
            if (pricelist.getId() == null) {
                pricelist.setId(100L);
            }
            return pricelist;
        });
        lenient().when(accessService.canCollaborate(any(Pricelist.class), eq(99L))).thenReturn(true);
        lenient().when(catalogService.findActiveVariantsByIds(anyCollection()))
                .thenReturn(Map.of(10L, new CatalogVariantDTO(10L, "Variant A", true)));
    }

    @Test
    void startWizardCreatesDraftIncompletePricelist() {
        var result = service.startWizard(99L);

        assertEquals(100L, result.getPricelistId());
        assertEquals(PricelistCreationStep.BASIC_INFO, result.getState().getCreationStep());
        assertFalse(result.getState().isCreationCompleted());
        assertEquals(PricelistStatus.DRAFT, result.getState().getStatus());
    }

    @Test
    void savingBasicInfoAdvancesStep() {
        Pricelist draft = draft(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));

        service.saveBasicInfo(100L, basicInfo(), 99L);

        assertEquals(PricelistCreationStep.TEAM_ACCESS, draft.getCreationStep());
        assertEquals("Pharmacy chains", draft.getCustomerSegment());
        assertFalse(draft.isCreationCompleted());
    }

    @Test
    void savingBasicInfoRejectsPastStartDate() {
        Pricelist draft = draft(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));
        SaveBasicInfoStepDTO dto = basicInfo();
        dto.setPeriodStart(dateAtStartOfDay(today().minusDays(1)));
        dto.setPeriodEnd(dateAtStartOfDay(today().plusDays(10)));

        assertThrows(PricelistStartDateInPastException.class, () -> service.saveBasicInfo(100L, dto, 99L));
    }

    @Test
    void savingTeamAccessRejectsDraftWithPastStartDate() {
        Pricelist draft = draft(100L);
        draft.setPeriodStart(dateAtStartOfDay(today().minusDays(1)));
        draft.setPeriodEnd(dateAtStartOfDay(today().plusDays(10)));
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));

        var dto = new com.example.iisdrugcrm.dto.pricelist.SaveTeamAccessStepDTO();

        assertThrows(PricelistStartDateInPastException.class, () -> service.saveTeamAccess(100L, dto, 99L));
    }

    @Test
    void savingItemsPersistsDraftItems() {
        Pricelist draft = draft(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));

        service.saveItems(100L, itemsDto(), 99L);

        assertEquals(PricelistCreationStep.THRESHOLDS, draft.getCreationStep());
        assertEquals(1, draft.getItems().size());
        assertEquals(10L, draft.getItems().get(0).getVariantId());
    }

    @Test
    void savingThresholdsValidatesContinuity() {
        Pricelist draft = draftWithItem(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));
        SaveThresholdsStepDTO dto = thresholdsDto();
        dto.getItems().get(0).getThresholds().get(1).setQuantityFrom(12);

        assertThrows(InvalidPricelistThresholdException.class, () -> service.saveThresholds(100L, dto, 99L));
    }

    @Test
    void finishWizardMarksDraftCompleted() {
        Pricelist draft = completeDraft(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));
        when(pricelistRepository.findOverlappingBlockingPricelistsExcludingCurrent(any(), any(), any(), any(), anyList(), any()))
                .thenReturn(List.of());

        service.finishWizard(100L, 99L);

        assertTrue(draft.isCreationCompleted());
        assertEquals(PricelistCreationStep.COMPLETED, draft.getCreationStep());
        verify(pricelistRepository).save(draft);
    }

    @Test
    void finishWizardRejectsDraftWithPastStartDate() {
        Pricelist draft = completeDraft(100L);
        draft.setPeriodStart(dateAtStartOfDay(today().minusDays(1)));
        draft.setPeriodEnd(dateAtStartOfDay(today().plusDays(10)));
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));

        assertThrows(PricelistStartDateInPastException.class, () -> service.finishWizard(100L, 99L));
    }

    @Test
    void userCannotEditSomeoneElsesDraft() {
        Pricelist draft = draft(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));
        when(accessService.canCollaborate(draft, 7L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.saveBasicInfo(100L, basicInfo(), 7L));
    }

    @Test
    void userCannotLoadSomeoneElsesDraftState() {
        Pricelist draft = draft(100L);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));
        when(accessService.canCollaborate(draft, 7L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getWizardState(100L, 7L));
    }

    @Test
    void draftsEndpointReturnsOnlyCurrentUsersUnfinishedDraftsFromRepository() {
        Pricelist draft = draft(100L);
        when(pricelistRepository.findAllByCreatedByAndCreationCompletedFalseOrderByLastEditedAtDescIdDesc(99L))
                .thenReturn(List.of(draft));

        var drafts = service.getDrafts(99L);

        assertEquals(1, drafts.size());
        assertEquals(100L, drafts.get(0).getPricelistId());
    }

    @Test
    void draftContinuationReturnsCurrentStep() {
        Pricelist draft = draftWithItem(100L);
        draft.setCreationStep(PricelistCreationStep.THRESHOLDS);
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));

        var state = service.getWizardState(100L, 99L);

        assertEquals(100L, state.getPricelistId());
        assertEquals(PricelistCreationStep.THRESHOLDS, state.getCreationStep());
        assertFalse(state.isCreationCompleted());
    }

    @Test
    void selectedTeamMustBelongToCurrentUser() {
        Pricelist draft = draft(100L);
        PricelistTeam team = new PricelistTeam("Team A", 7L);
        team.setId(5L);
        team.setMemberIds(Set.of(8L));
        when(pricelistRepository.findById(100L)).thenReturn(Optional.of(draft));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));

        var dto = new com.example.iisdrugcrm.dto.pricelist.SaveTeamAccessStepDTO();
        dto.setTeamId(5L);

        assertThrows(AccessDeniedException.class, () -> service.saveTeamAccess(100L, dto, 99L));
    }

    private SaveBasicInfoStepDTO basicInfo() {
        SaveBasicInfoStepDTO dto = new SaveBasicInfoStepDTO();
        dto.setRegionId(1L);
        dto.setCustomerSegment("Pharmacy chains");
        dto.setCurrency("RSD");
        dto.setPeriodStart(dateAtStartOfDay(today().plusDays(1)));
        dto.setPeriodEnd(dateAtStartOfDay(today().plusDays(90)));
        return dto;
    }

    private SaveItemsStepDTO itemsDto() {
        SaveItemsStepDTO dto = new SaveItemsStepDTO();
        SaveItemsStepDTO.PricelistWizardItemDTO item = new SaveItemsStepDTO.PricelistWizardItemDTO();
        item.setVariantId(10L);
        item.setVariantName("Variant A");
        dto.setItems(List.of(item));
        return dto;
    }

    private SaveThresholdsStepDTO thresholdsDto() {
        SaveThresholdsStepDTO dto = new SaveThresholdsStepDTO();
        SaveThresholdsStepDTO.PricelistItemThresholdsDTO item = new SaveThresholdsStepDTO.PricelistItemThresholdsDTO();
        item.setVariantId(10L);
        item.setThresholds(List.of(
                threshold(1, 10, "100.00"),
                threshold(11, null, "95.00")
        ));
        dto.setItems(List.of(item));
        return dto;
    }

    private SaveThresholdsStepDTO.QuantityThresholdDTO threshold(int quantityFrom, Integer quantityTo, String price) {
        SaveThresholdsStepDTO.QuantityThresholdDTO threshold = new SaveThresholdsStepDTO.QuantityThresholdDTO();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }

    private Pricelist draft(Long id) {
        Pricelist pricelist = new Pricelist();
        pricelist.setId(id);
        pricelist.setRegion(serbia);
        pricelist.setCustomerSegment("UNDEFINED");
        pricelist.setCurrency("RSD");
        pricelist.setStatus(PricelistStatus.DRAFT);
        pricelist.setPeriodStart(dateAtStartOfDay(today().plusDays(1)));
        pricelist.setPeriodEnd(dateAtStartOfDay(today().plusDays(90)));
        pricelist.setCreatedBy(99L);
        pricelist.setCreationStep(PricelistCreationStep.BASIC_INFO);
        pricelist.setCreationCompleted(false);
        return pricelist;
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.systemDefault());
    }

    private OffsetDateTime dateAtStartOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private Pricelist draftWithItem(Long id) {
        Pricelist pricelist = draft(id);
        PricelistItem item = new PricelistItem();
        item.setVariantId(10L);
        item.setVariantName("Variant A");
        pricelist.addItem(item);
        return pricelist;
    }

    private Pricelist completeDraft(Long id) {
        Pricelist pricelist = draftWithItem(id);
        pricelist.setCustomerSegment("Pharmacy chains");
        pricelist.getItems().get(0).setThresholds(List.of(quantityThreshold(1, 10, "100.00"), quantityThreshold(11, null, "95.00")));
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
