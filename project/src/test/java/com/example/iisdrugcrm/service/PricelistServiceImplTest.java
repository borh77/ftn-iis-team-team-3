package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.exception.InvalidPricelistThresholdException;
import com.example.iisdrugcrm.exception.PricelistConflictException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.RegionRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    private PricelistServiceImpl service;
    private Region serbia;

    @BeforeEach
    void setUp() {
        service = new PricelistServiceImpl(pricelistRepository, regionRepository, catalogService);
        serbia = region(1L, "Srbija", "RS");
        lenient().when(regionRepository.findById(1L)).thenReturn(Optional.of(serbia));
        when(catalogService.findActiveVariantsByIds(anyCollection()))
                .thenReturn(Map.of(10L, new CatalogVariantDTO(10L, "Variant A", true)));
        lenient().when(pricelistRepository.save(any(Pricelist.class))).thenAnswer(invocation -> invocation.getArgument(0));
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

    private void noBlockingConflict() {
        when(pricelistRepository.findOverlappingBlockingPricelists(any(), any(), any(), any(), anyList()))
                .thenReturn(List.of());
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
        Pricelist pricelist = new Pricelist();
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(customerSegment);
        pricelist.setCurrency("RSD");
        pricelist.setStatus(status);
        pricelist.setPeriodStart(OffsetDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        pricelist.setPeriodEnd(OffsetDateTime.of(2026, 9, 30, 0, 0, 0, 0, ZoneOffset.UTC));
        return pricelist;
    }

    private Region region(Long id, String name, String code) {
        Region region = new Region(name, code);
        region.setId(id);
        return region;
    }
}
