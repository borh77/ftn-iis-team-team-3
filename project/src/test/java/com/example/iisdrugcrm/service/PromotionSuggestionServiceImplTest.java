package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import com.example.iisdrugcrm.dto.pricelist.PromotionSuggestionDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionSuggestionServiceImplTest {

    @Mock
    private PricelistRepository pricelistRepository;
    @Mock
    private SpecialOfferRepository specialOfferRepository;

    private PromotionSuggestionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PromotionSuggestionServiceImpl(pricelistRepository, specialOfferRepository);
    }

    @Test
    void blankSegmentIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.getSuggestions(" "));
    }

    @Test
    void returnsDeterministicFallbackSuggestionsForActiveSegmentPricelists() {
        Pricelist pricelist = pricelist("Pharmacy chains",
                item(101L, "Medicine A", "100.00"),
                item(102L, "Medicine B", "700.00"));
        when(pricelistRepository.findActivePricelistsByCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of());
        when(specialOfferRepository.findAllOffersForCustomerSegment("Pharmacy chains")).thenReturn(List.of());

        List<PromotionSuggestionDTO> suggestions = service.getSuggestions(" Pharmacy chains ");

        assertEquals(2, suggestions.size());
        assertEquals(102L, suggestions.get(0).getVariantId());
        assertEquals(DiscountType.PERCENTAGE, suggestions.get(0).getSuggestedDiscountType());
        assertEquals(new BigDecimal("10.00"), suggestions.get(0).getSuggestedDiscountValue());
        assertEquals("ACTIVE_PRICELIST_HEURISTIC", suggestions.get(0).getSource());
        verify(pricelistRepository).findActivePricelistsByCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class));
    }

    @Test
    void excludesVariantsAlreadyCoveredByActiveOffers() {
        Pricelist pricelist = pricelist("Pharmacy chains",
                item(101L, "Medicine A", "100.00"),
                item(102L, "Medicine B", "700.00"));
        when(pricelistRepository.findActivePricelistsByCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of(offer(pricelist, 102L, SpecialOfferStatus.ACTIVE)));
        when(specialOfferRepository.findAllOffersForCustomerSegment("Pharmacy chains")).thenReturn(List.of());

        List<PromotionSuggestionDTO> suggestions = service.getSuggestions("Pharmacy chains");

        assertEquals(1, suggestions.size());
        assertEquals(101L, suggestions.get(0).getVariantId());
    }

    @Test
    void previousOfferHistoryUsesConservativeDiscount() {
        Pricelist pricelist = pricelist("Pharmacy chains", item(101L, "Medicine A", "100.00"));
        when(pricelistRepository.findActivePricelistsByCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of());
        when(specialOfferRepository.findAllOffersForCustomerSegment("Pharmacy chains"))
                .thenReturn(List.of(offer(pricelist, 101L, SpecialOfferStatus.ARCHIVED)));

        List<PromotionSuggestionDTO> suggestions = service.getSuggestions("Pharmacy chains");

        assertEquals(1, suggestions.size());
        assertEquals(new BigDecimal("5.00"), suggestions.get(0).getSuggestedDiscountValue());
        assertEquals("SPECIAL_OFFER_HISTORY", suggestions.get(0).getSource());
    }

    @Test
    void returnsAtMostFiveSuggestions() {
        Pricelist pricelist = pricelist("Pharmacy chains",
                item(101L, "Medicine A", "100.00"),
                item(102L, "Medicine B", "200.00"),
                item(103L, "Medicine C", "300.00"),
                item(104L, "Medicine D", "400.00"),
                item(105L, "Medicine E", "500.00"),
                item(106L, "Medicine F", "600.00"));
        when(pricelistRepository.findActivePricelistsByCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of(pricelist));
        when(specialOfferRepository.findActiveOffersForCustomerSegment(eq("Pharmacy chains"), any(OffsetDateTime.class)))
                .thenReturn(List.of());
        when(specialOfferRepository.findAllOffersForCustomerSegment("Pharmacy chains")).thenReturn(List.of());

        List<PromotionSuggestionDTO> suggestions = service.getSuggestions("Pharmacy chains");

        assertEquals(5, suggestions.size());
    }

    private Pricelist pricelist(String customerSegment, PricelistItem... items) {
        Region region = new Region("Srbija", "RS");
        region.setId(1L);
        Pricelist pricelist = new Pricelist();
        pricelist.setId(1L);
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(customerSegment);
        pricelist.setCurrency("RSD");
        pricelist.setStatus(PricelistStatus.ACTIVE);
        pricelist.setPeriodStart(OffsetDateTime.now().minusDays(1));
        pricelist.setPeriodEnd(OffsetDateTime.now().plusDays(30));
        for (PricelistItem item : items) {
            pricelist.addItem(item);
        }
        return pricelist;
    }

    private PricelistItem item(Long variantId, String variantName, String basePrice) {
        PricelistItem item = new PricelistItem();
        item.setVariantId(variantId);
        item.setVariantName(variantName);
        item.setThresholds(List.of(threshold(basePrice)));
        return item;
    }

    private QuantityThreshold threshold(String price) {
        QuantityThreshold threshold = new QuantityThreshold();
        threshold.setQuantityFrom(1);
        threshold.setQuantityTo(null);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }

    private SpecialOffer offer(Pricelist pricelist, Long variantId, SpecialOfferStatus status) {
        SpecialOffer offer = new SpecialOffer();
        offer.setPricelist(pricelist);
        offer.setVariantId(variantId);
        offer.setVariantName("Medicine " + variantId);
        offer.setDiscountType(DiscountType.PERCENTAGE);
        offer.setDiscountValue(new BigDecimal("10.00"));
        offer.setStartDate(OffsetDateTime.now().minusDays(1));
        offer.setEndDate(OffsetDateTime.now().plusDays(1));
        offer.setStatus(status);
        offer.setCreatedAt(OffsetDateTime.now().minusDays(10));
        return offer;
    }
}
