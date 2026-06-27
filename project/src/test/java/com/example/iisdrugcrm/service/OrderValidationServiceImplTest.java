package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import com.example.iisdrugcrm.dto.order.OrderDocumentItemDTO;
import com.example.iisdrugcrm.dto.order.ValidationResultDTO;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.service.order.OrderDocumentParser;
import com.example.iisdrugcrm.service.order.OrderDocumentParserResolver;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderValidationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PricelistRepository pricelistRepository;

    @Mock
    private SpecialOfferRepository specialOfferRepository;

    @Mock
    private CatalogService catalogService;

    @Mock
    private OrderDocumentParserResolver parserResolver;

    @Mock
    private OrderDocumentParser parser;

    private OrderValidationServiceImpl service;
    private MockMultipartFile file;
    private User buyer;
    private Pricelist pricelist;

    @BeforeEach
    void setUp() throws Exception {
        service = new OrderValidationServiceImpl(userRepository, pricelistRepository, specialOfferRepository, catalogService, parserResolver);
        file = new MockMultipartFile("file", "order.json", "application/json", "[]".getBytes());
        Region region = region(1L, "Srbija", "RS");
        buyer = buyer("buyer", region, "Pharmacy chains");
        pricelist = pricelist(10L, region, "Pharmacy chains");

        when(parserResolver.resolve(file)).thenReturn(parser);
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of(pricelist));
        lenient().when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any())).thenReturn(List.of());
    }

    @Test
    void quantityThresholdSelectionUsesMatchingRange() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(101L, 55)));
        when(catalogService.findActiveVariantsByIds(List.of(101L)))
                .thenReturn(Map.of(101L, new CatalogVariantDTO(101L, "Medicine A 10mg", true)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertTrue(result.isValid());
        assertEquals(new BigDecimal("90.00"), result.getValidatedItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("4950.00"), result.getTotalPrice());
    }

    @Test
    void itemNotInActivePricelistBecomesInvalidItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(202L, 10)));
        when(catalogService.findActiveVariantsByIds(List.of(202L)))
                .thenReturn(Map.of(202L, new CatalogVariantDTO(202L, "Medicine B 20mg", true)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(0, result.getValidatedItems().size());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("VARIANT_NOT_IN_PRICELIST", result.getInvalidItems().get(0).getErrorCode());
        assertEquals(new BigDecimal("0.00"), result.getTotalPrice());
    }

    @Test
    void validItemsContributeToTotalAndInvalidItemsDoNot() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(
                new OrderDocumentItemDTO(101L, 10),
                new OrderDocumentItemDTO(101L, -1)
        ));
        when(catalogService.findActiveVariantsByIds(List.of(101L, 101L)))
                .thenReturn(Map.of(101L, new CatalogVariantDTO(101L, "Medicine A 10mg", true)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getValidatedItems().size());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals(new BigDecimal("1000.00"), result.getTotalPrice());
    }

    @Test
    void activePercentageOfferAppliesToSelectedThresholdPrice() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(101L, 55)));
        when(catalogService.findActiveVariantsByIds(List.of(101L)))
                .thenReturn(Map.of(101L, new CatalogVariantDTO(101L, "Medicine A 10mg", true)));
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any()))
                .thenReturn(List.of(offer(pricelist, DiscountType.PERCENTAGE, "10.00")));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertTrue(result.isValid());
        assertEquals(DiscountType.PERCENTAGE, result.getValidatedItems().get(0).getDiscountType());
        assertEquals(new BigDecimal("81.00"), result.getValidatedItems().get(0).getFinalUnitPrice());
        assertEquals(new BigDecimal("4455.00"), result.getTotalPrice());
    }

    private User buyer(String username, Region region, String customerSegment) {
        User user = new User();
        user.setUsername(username);
        user.setRole(UserRole.ROLE_BUYER);
        user.setBuyerRegion(region);
        user.setCustomerSegment(customerSegment);
        return user;
    }

    private Pricelist pricelist(Long id, Region region, String customerSegment) {
        Pricelist pricelist = new Pricelist();
        pricelist.setId(id);
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(customerSegment);
        pricelist.setCurrency("RSD");
        pricelist.setStatus(PricelistStatus.ACTIVE);
        pricelist.setPeriodStart(OffsetDateTime.of(2026, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        pricelist.setPeriodEnd(OffsetDateTime.of(2026, 9, 30, 0, 0, 0, 0, ZoneOffset.UTC));

        PricelistItem item = new PricelistItem();
        item.setVariantId(101L);
        item.setVariantName("Medicine A 10mg");
        item.setThresholds(List.of(
                threshold(1, 10, "100.00"),
                threshold(11, 50, "95.00"),
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

    private SpecialOffer offer(Pricelist pricelist, DiscountType type, String value) {
        SpecialOffer offer = new SpecialOffer();
        offer.setPricelist(pricelist);
        offer.setVariantId(101L);
        offer.setVariantName("Medicine A 10mg");
        offer.setDiscountType(type);
        offer.setDiscountValue(new BigDecimal(value));
        offer.setStatus(SpecialOfferStatus.ACTIVE);
        return offer;
    }

    private Region region(Long id, String name, String code) {
        Region region = new Region(name, code);
        region.setId(id);
        return region;
    }
}
