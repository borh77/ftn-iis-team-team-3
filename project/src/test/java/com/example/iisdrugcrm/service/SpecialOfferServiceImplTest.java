package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import com.example.iisdrugcrm.dto.pricelist.CreateSpecialOfferDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialOfferServiceImplTest {

    @Mock
    private SpecialOfferRepository specialOfferRepository;
    @Mock
    private PricelistRepository pricelistRepository;
    @Mock
    private PricelistAccessService accessService;

    private SpecialOfferServiceImpl service;
    private Pricelist pricelist;

    @BeforeEach
    void setUp() {
        service = new SpecialOfferServiceImpl(specialOfferRepository, pricelistRepository, accessService);
        pricelist = pricelist(PricelistStatus.ACTIVE);
        lenient().when(specialOfferRepository.save(any(SpecialOffer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(specialOfferRepository.findAllByPricelistIdOrderByIdDesc(any())).thenReturn(List.of());
    }

    @Test
    void createPercentageOfferSucceeds() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        service.createOffer(dto(DiscountType.PERCENTAGE, "10.00", 101L), 99L);
    }

    @Test
    void createFixedAmountOfferSucceeds() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        service.createOffer(dto(DiscountType.FIXED_AMOUNT, "500.00", 101L), 99L);
    }

    @Test
    void invalidPercentageGreaterThan100Fails() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        assertThrows(IllegalArgumentException.class, () -> service.createOffer(dto(DiscountType.PERCENTAGE, "101.00", 101L), 99L));
    }

    @Test
    void discountValueLessThanOrEqualToZeroFails() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        assertThrows(IllegalArgumentException.class, () -> service.createOffer(dto(DiscountType.FIXED_AMOUNT, "0.00", 101L), 99L));
    }

    @Test
    void offerPeriodOutsidePricelistPeriodFails() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));
        CreateSpecialOfferDTO dto = dto(DiscountType.PERCENTAGE, "10.00", 101L);
        dto.setStartDate(OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC));

        assertThrows(IllegalArgumentException.class, () -> service.createOffer(dto, 99L));
    }

    @Test
    void selectedVariantNotInPricelistFails() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        assertThrows(IllegalArgumentException.class, () -> service.createOffer(dto(DiscountType.PERCENTAGE, "10.00", 999L), 99L));
    }

    @Test
    void archivedPricelistCannotReceiveOffer() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist(PricelistStatus.ARCHIVED)));

        assertThrows(IllegalArgumentException.class, () -> service.createOffer(dto(DiscountType.PERCENTAGE, "10.00", 101L), 99L));
    }

    @Test
    void validPercentageOfferActivates() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        service.activateOffer(5L, 99L);

        assertEquals(SpecialOfferStatus.ACTIVE, offer.getStatus());
    }

    @Test
    void validFixedAmountOfferActivates() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        offer.setDiscountType(DiscountType.FIXED_AMOUNT);
        offer.setDiscountValue(new BigDecimal("50.00"));
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        service.activateOffer(5L, 99L);

        assertEquals(SpecialOfferStatus.ACTIVE, offer.getStatus());
    }

    @Test
    void activationFailsIfParentPricelistIsArchived() {
        pricelist = pricelist(PricelistStatus.ARCHIVED);
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void activeToArchivedOfferSucceeds() {
        SpecialOffer offer = offer(SpecialOfferStatus.ACTIVE);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        service.archiveOffer(5L, 99L);

        assertEquals(SpecialOfferStatus.ARCHIVED, offer.getStatus());
    }

    @Test
    void activationFailsIfPercentageDiscountIsGreaterThan100() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        offer.setDiscountValue(new BigDecimal("101.00"));
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void activationFailsIfDiscountValueIsNotPositive() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        offer.setDiscountValue(BigDecimal.ZERO);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void activationFailsIfFixedDiscountIsGreaterThanBasePrice() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        offer.setDiscountType(DiscountType.FIXED_AMOUNT);
        offer.setDiscountValue(new BigDecimal("150.00"));
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void activationFailsIfSelectedItemHasNoThresholds() {
        pricelist.getItems().get(0).setThresholds(List.of());
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void activationFailsIfSelectedVariantNoLongerExistsInPricelist() {
        pricelist.setItems(List.of());
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void activationFailsIfOfferPeriodIsOutsidePricelistPeriod() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        offer.setStartDate(OffsetDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.DRAFT, offer.getStatus());
    }

    @Test
    void archivedOfferCannotBeActivatedOrArchivedAgain() {
        SpecialOffer offer = offer(SpecialOfferStatus.ARCHIVED);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertThrows(IllegalArgumentException.class, () -> service.archiveOffer(5L, 99L));
    }

    @Test
    void activeOfferCannotBeActivatedAgain() {
        SpecialOffer offer = offer(SpecialOfferStatus.ACTIVE);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 99L));
        assertEquals(SpecialOfferStatus.ACTIVE, offer.getStatus());
    }

    @Test
    void teammateCanCreateOfferForTeamPricelist() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        service.createOffer(dto(DiscountType.PERCENTAGE, "10.00", 101L), 7L);
    }

    @Test
    void teammateCanListOffersForTeamPricelist() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));

        service.listOffersForPricelist(1L, 7L);
    }

    @Test
    void unrelatedCreatorCannotCreateOffer() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));
        doThrow(new IllegalArgumentException("You do not have access to this pricelist."))
                .when(accessService).validateOwnerOrTeamMember(pricelist, 7L);

        assertThrows(IllegalArgumentException.class, () -> service.createOffer(dto(DiscountType.PERCENTAGE, "10.00", 101L), 7L));
    }

    @Test
    void unrelatedCreatorCannotListOffers() {
        when(pricelistRepository.findById(1L)).thenReturn(Optional.of(pricelist));
        doThrow(new IllegalArgumentException("You do not have access to this pricelist."))
                .when(accessService).validateOwnerOrTeamMember(pricelist, 7L);

        assertThrows(IllegalArgumentException.class, () -> service.listOffersForPricelist(1L, 7L));
    }

    @Test
    void teammateCannotActivateAnotherCreatorsOffer() {
        SpecialOffer offer = offer(SpecialOfferStatus.DRAFT);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));
        doThrow(new IllegalArgumentException("Only the owner can change this pricelist status."))
                .when(accessService).validateOwnerOnly(pricelist, 7L);

        assertThrows(IllegalArgumentException.class, () -> service.activateOffer(5L, 7L));
    }

    @Test
    void teammateCannotArchiveAnotherCreatorsOffer() {
        SpecialOffer offer = offer(SpecialOfferStatus.ACTIVE);
        when(specialOfferRepository.findById(5L)).thenReturn(Optional.of(offer));
        doThrow(new IllegalArgumentException("Only the owner can change this pricelist status."))
                .when(accessService).validateOwnerOnly(pricelist, 7L);

        assertThrows(IllegalArgumentException.class, () -> service.archiveOffer(5L, 7L));
    }

    private CreateSpecialOfferDTO dto(DiscountType type, String value, Long variantId) {
        CreateSpecialOfferDTO dto = new CreateSpecialOfferDTO();
        dto.setPricelistId(1L);
        dto.setVariantId(variantId);
        dto.setDiscountType(type);
        dto.setDiscountValue(new BigDecimal(value));
        dto.setStartDate(OffsetDateTime.of(2026, 7, 5, 0, 0, 0, 0, ZoneOffset.UTC));
        dto.setEndDate(OffsetDateTime.of(2026, 8, 5, 0, 0, 0, 0, ZoneOffset.UTC));
        return dto;
    }

    private SpecialOffer offer(SpecialOfferStatus status) {
        SpecialOffer offer = new SpecialOffer();
        offer.setPricelist(pricelist);
        offer.setVariantId(101L);
        offer.setVariantName("Medicine A");
        offer.setDiscountType(DiscountType.PERCENTAGE);
        offer.setDiscountValue(new BigDecimal("10.00"));
        offer.setStartDate(OffsetDateTime.of(2026, 7, 5, 0, 0, 0, 0, ZoneOffset.UTC));
        offer.setEndDate(OffsetDateTime.of(2026, 8, 5, 0, 0, 0, 0, ZoneOffset.UTC));
        offer.setStatus(status);
        return offer;
    }

    private Pricelist pricelist(PricelistStatus status) {
        Region region = new Region("Srbija", "RS");
        region.setId(1L);
        Pricelist pricelist = new Pricelist();
        pricelist.setId(1L);
        pricelist.setRegion(region);
        pricelist.setCustomerSegment("Pharmacy chains");
        pricelist.setCurrency("RSD");
        pricelist.setStatus(status);
        pricelist.setCreatedBy(99L);
        pricelist.setPeriodStart(OffsetDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        pricelist.setPeriodEnd(OffsetDateTime.of(2026, 9, 30, 0, 0, 0, 0, ZoneOffset.UTC));
        PricelistItem item = new PricelistItem();
        item.setVariantId(101L);
        item.setVariantName("Medicine A");
        item.setThresholds(List.of(
                threshold(11, 50, "95.00"),
                threshold(1, 10, "100.00")
        ));
        pricelist.addItem(item);
        return pricelist;
    }

    private QuantityThreshold threshold(Integer quantityFrom, Integer quantityTo, String price) {
        QuantityThreshold threshold = new QuantityThreshold();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }
}
