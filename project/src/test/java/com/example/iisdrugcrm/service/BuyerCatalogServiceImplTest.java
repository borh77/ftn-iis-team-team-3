package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import com.example.iisdrugcrm.dto.pricelist.BuyerCatalogDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import com.example.iisdrugcrm.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BuyerCatalogServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PricelistRepository pricelistRepository;

    @Mock
    private SpecialOfferRepository specialOfferRepository;

    private BuyerCatalogServiceImpl service;
    private Region serbia;

    @BeforeEach
    void setUp() {
        service = new BuyerCatalogServiceImpl(userRepository, pricelistRepository, specialOfferRepository);
        serbia = region(1L, "Srbija", "RS");
        lenient().when(specialOfferRepository.findActiveOffersForPricelist(any(), any())).thenReturn(List.of());
    }

    @Test
    void buyerWithMatchingRegionAndSegmentSeesActivePricelist() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        Pricelist pricelist = pricelist(10L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of(pricelist));

        BuyerCatalogDTO catalog = service.getCatalogForBuyer("buyer");

        assertEquals(10L, catalog.getPricelistId());
        assertEquals("Srbija", catalog.getRegionName());
        assertEquals(1, catalog.getItems().size());
    }

    @Test
    void draftInReviewArchivedOutsidePeriodDifferentRegionAndDifferentSegmentAreIgnoredByRepositoryLookup() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of());

        BuyerCatalogDTO catalog = service.getCatalogForBuyer("buyer");

        assertNull(catalog.getPricelistId());
        assertEquals(0, catalog.getItems().size());
    }

    @Test
    void multipleActivePricelistsUsesFirstRepositoryResult() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        Pricelist newest = pricelist(12L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        Pricelist older = pricelist(11L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of(newest, older));

        BuyerCatalogDTO catalog = service.getCatalogForBuyer("buyer");

        assertEquals(12L, catalog.getPricelistId());
    }

    @Test
    void noMatchingActivePricelistReturnsEmptyCatalog() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of());

        BuyerCatalogDTO catalog = service.getCatalogForBuyer("buyer");

        assertNull(catalog.getPricelistId());
        assertEquals(0, catalog.getItems().size());
    }

    @Test
    void endpointServiceUsesAuthenticatedUsername() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of());

        service.getCatalogForBuyer("buyer");

        verify(userRepository).findByUsername("buyer");
    }

    @Test
    void buyerWithoutRegionOrSegmentGetsEmptyCatalog() {
        User buyer = buyer("buyer", null, null);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));

        BuyerCatalogDTO catalog = service.getCatalogForBuyer("buyer");

        assertNull(catalog.getPricelistId());
        assertEquals(0, catalog.getItems().size());
        verify(pricelistRepository, never()).findActiveBuyerPricelists(any(), any(), any());
    }

    @Test
    void nonBuyerCannotUseBuyerCatalogService() {
        User user = buyer("creator", serbia, "Pharmacy chains");
        user.setRole(UserRole.ROLE_PRICELIST_CREATOR);
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.getCatalogForBuyer("creator"));
    }

    @Test
    void basePriceComesFromLowestQuantityThresholdAndAllThresholdsAreReturned() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        Pricelist pricelist = pricelist(10L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of(pricelist));

        BuyerCatalogDTO catalog = service.getCatalogForBuyer("buyer");

        BuyerCatalogDTO.BuyerCatalogItemDTO item = catalog.getItems().get(0);
        assertEquals(new BigDecimal("100.00"), item.getBasePrice());
        assertEquals(3, item.getThresholds().size());
    }

    @Test
    void activePercentageOfferAppliesDiscountedPrice() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        Pricelist pricelist = pricelist(10L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any())).thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any())).thenReturn(List.of(offer(pricelist, DiscountType.PERCENTAGE, "10.00", SpecialOfferStatus.ACTIVE)));

        BuyerCatalogDTO.BuyerCatalogItemDTO item = service.getCatalogForBuyer("buyer").getItems().get(0);

        assertEquals(new BigDecimal("90.00"), item.getDiscountedPrice());
        assertEquals(true, item.isHasActiveOffer());
    }

    @Test
    void activeFixedOfferAppliesDiscountedPriceAndDoesNotGoBelowZero() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        Pricelist pricelist = pricelist(10L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any())).thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any())).thenReturn(List.of(offer(pricelist, DiscountType.FIXED_AMOUNT, "500.00", SpecialOfferStatus.ACTIVE)));

        BuyerCatalogDTO.BuyerCatalogItemDTO item = service.getCatalogForBuyer("buyer").getItems().get(0);

        assertEquals(new BigDecimal("0.00"), item.getDiscountedPrice());
        assertEquals(true, item.isHasActiveOffer());
    }

    @Test
    void draftExpiredAndArchivedOffersAreIgnoredByRepositoryLookup() {
        User buyer = buyer("buyer", serbia, "Pharmacy chains");
        Pricelist pricelist = pricelist(10L, PricelistStatus.ACTIVE, serbia, "Pharmacy chains");
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any())).thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any())).thenReturn(List.of());

        BuyerCatalogDTO.BuyerCatalogItemDTO item = service.getCatalogForBuyer("buyer").getItems().get(0);

        assertEquals(false, item.isHasActiveOffer());
        assertNull(item.getDiscountedPrice());
    }

    private User buyer(String username, Region region, String customerSegment) {
        User user = new User();
        user.setUsername(username);
        user.setRole(UserRole.ROLE_BUYER);
        user.setBuyerRegion(region);
        user.setCustomerSegment(customerSegment);
        return user;
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

        PricelistItem item = new PricelistItem();
        item.setVariantId(101L);
        item.setVariantName("Medicine A 10mg");
        item.setThresholds(List.of(
                threshold(11, 50, "95.00"),
                threshold(1, 10, "100.00"),
                threshold(51, null, "90.00")
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

    private SpecialOffer offer(Pricelist pricelist, DiscountType type, String value, SpecialOfferStatus status) {
        SpecialOffer offer = new SpecialOffer();
        offer.setPricelist(pricelist);
        offer.setVariantId(101L);
        offer.setVariantName("Medicine A 10mg");
        offer.setDiscountType(type);
        offer.setDiscountValue(new BigDecimal(value));
        offer.setStatus(status);
        return offer;
    }

    private Region region(Long id, String name, String code) {
        Region region = new Region(name, code);
        region.setId(id);
        return region;
    }
}
