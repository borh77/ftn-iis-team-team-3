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
import com.example.iisdrugcrm.dto.order.ReplacementSuggestionDTO;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        file = new MockMultipartFile("file", "order.csv", "text/csv", "variantId,requestedQuantity\n".getBytes());
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
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(101L, catalogVariant(101L, "Medicine A 10mg", true, null, null)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertTrue(result.isValid());
        assertEquals(new BigDecimal("90.00"), result.getValidatedItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("4950.00"), result.getTotalPrice());
    }

    @Test
    void unsupportedJsonUploadFailsBeforeBusinessValidation() {
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file",
                "order.json",
                "application/json",
                "[{\"variantId\":2,\"requestedQuantity\":10}]".getBytes()
        );
        when(parserResolver.resolve(jsonFile))
                .thenThrow(new IllegalArgumentException("Only CSV procurement documents are currently supported."));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateOrderDocument("buyer", jsonFile)
        );

        assertEquals("Only CSV procurement documents are currently supported.", exception.getMessage());
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void itemNotInActivePricelistBecomesInvalidItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(202L, 10)));
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(202L, catalogVariant(202L, "Medicine B 20mg", true, null, null)));

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
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(101L, catalogVariant(101L, "Medicine A 10mg", true, null, null)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getValidatedItems().size());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals(new BigDecimal("1000.00"), result.getTotalPrice());
    }

    @Test
    void activePercentageOfferAppliesToSelectedThresholdPrice() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(101L, 55)));
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(101L, catalogVariant(101L, "Medicine A 10mg", true, null, null)));
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any()))
                .thenReturn(List.of(offer(pricelist, 101L, DiscountType.PERCENTAGE, "10.00")));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertTrue(result.isValid());
        assertEquals(DiscountType.PERCENTAGE, result.getValidatedItems().get(0).getDiscountType());
        assertEquals(new BigDecimal("81.00"), result.getValidatedItems().get(0).getFinalUnitPrice());
        assertEquals(new BigDecimal("4455.00"), result.getTotalPrice());
    }

    @Test
    void variantNameResolvesToExistingVariantBeforeValidation() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(" medicine a 10mg ", 10)));
        when(catalogService.findVariantsByDisplayNameIncludingInactive(" medicine a 10mg "))
                .thenReturn(List.of(catalogVariant(101L, "Medicine A 10mg", true, null, null)));
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(101L, catalogVariant(101L, "Medicine A 10mg", true, null, null)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertTrue(result.isValid());
        assertEquals(101L, result.getValidatedItems().get(0).getVariantId());
        assertEquals("Medicine A 10mg", result.getValidatedItems().get(0).getVariantName());
    }

    @Test
    void structuredProductFieldsResolveToExistingVariantBeforeValidation() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO("Medicine A", "tablet", "10mg", 10)));
        when(catalogService.findVariantsByProductFormDosageIncludingInactive("Medicine A", "tablet", "10mg"))
                .thenReturn(List.of(catalogVariant(101L, "Medicine A 10mg", true, null, null)));
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(101L, catalogVariant(101L, "Medicine A 10mg", true, null, null)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertTrue(result.isValid());
        assertEquals(101L, result.getValidatedItems().get(0).getVariantId());
    }

    @Test
    void unknownVariantNameReturnsProblematicItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO("Wrong Medicine", 10)));
        when(catalogService.findVariantsByDisplayNameIncludingInactive("Wrong Medicine"))
                .thenReturn(List.of());

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("VARIANT_NOT_FOUND", result.getInvalidItems().get(0).getErrorCode());
        assertEquals("Wrong Medicine", result.getInvalidItems().get(0).getVariantName());
        assertEquals("Variant was not found in the catalog.", result.getInvalidItems().get(0).getMessage());
    }

    @Test
    void variantNameWithInvalidQuantityReturnsProblematicItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO("Brufen LIQUID 400mg", 0)));
        when(catalogService.findVariantsByDisplayNameIncludingInactive("Brufen LIQUID 400mg"))
                .thenReturn(List.of(catalogVariant(101L, "Brufen LIQUID 400mg", true, null, null)));
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(101L, catalogVariant(101L, "Brufen LIQUID 400mg", true, null, null)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("INVALID_QUANTITY", result.getInvalidItems().get(0).getErrorCode());
        assertEquals("Brufen LIQUID 400mg", result.getInvalidItems().get(0).getVariantName());
        assertEquals(0, result.getInvalidItems().get(0).getRequestedQuantity());
    }

    @Test
    void ambiguousVariantNameReturnsProblematicItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO("Medicine A 10mg", 10)));
        when(catalogService.findVariantsByDisplayNameIncludingInactive("Medicine A 10mg"))
                .thenReturn(List.of(
                        catalogVariant(101L, "Medicine A 10mg", true, null, null),
                        catalogVariant(102L, "Medicine A 10mg", true, null, null)
                ));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("AMBIGUOUS_VARIANT_NAME", result.getInvalidItems().get(0).getErrorCode());
    }

    @Test
    void discontinuedVariantResolvedByNameReturnsReplacementSuggestion() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO("Old Medicine 10mg", 55)));
        when(catalogService.findVariantsByDisplayNameIncludingInactive("Old Medicine 10mg"))
                .thenReturn(List.of(catalogVariant(202L, "Old Medicine 10mg", false, 303L, "Replacement Medicine 20mg")));
        when(catalogService.findVariantsByIdsIncludingInactive(any()))
                .thenReturn(
                        Map.of(202L, catalogVariant(202L, "Old Medicine 10mg", false, 303L, "Replacement Medicine 20mg")),
                        Map.of(303L, catalogVariant(303L, "Replacement Medicine 20mg", true, null, null))
                );

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getReplacements().size());
        ReplacementSuggestionDTO replacement = result.getReplacements().get(0);
        assertEquals(202L, replacement.getOldVariantId());
        assertEquals("Old Medicine 10mg", replacement.getOldVariantName());
        assertEquals(303L, replacement.getNewVariantId());
        assertEquals(55, replacement.getRequestedQuantity());
    }

    @Test
    void archivedAdvilVariantResolvedByNameReturnsReplacementSuggestion() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO("Advil TABLET 400mg", 2)));
        when(catalogService.findVariantsByDisplayNameIncludingInactive("Advil TABLET 400mg"))
                .thenReturn(List.of(catalogVariant(202L, "Advil TABLET 400mg", false, 303L, "Replacement Medicine 20mg")));
        when(catalogService.findVariantsByIdsIncludingInactive(any()))
                .thenReturn(
                        Map.of(202L, catalogVariant(202L, "Advil TABLET 400mg", false, 303L, "Replacement Medicine 20mg")),
                        Map.of(303L, catalogVariant(303L, "Replacement Medicine 20mg", true, null, null))
                );

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(1, result.getReplacements().size());
        ReplacementSuggestionDTO replacement = result.getReplacements().get(0);
        assertEquals(202L, replacement.getOldVariantId());
        assertEquals("Advil TABLET 400mg", replacement.getOldVariantName());
        assertEquals(303L, replacement.getNewVariantId());
        assertEquals(2, replacement.getRequestedQuantity());
    }

    @Test
    void discontinuedVariantWithValidSuccessorReturnsReplacementSuggestion() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(202L, 55)));
        when(catalogService.findVariantsByIdsIncludingInactive(any()))
                .thenReturn(
                        Map.of(202L, catalogVariant(202L, "Old Medicine 10mg", false, 303L, "Replacement Medicine 20mg")),
                        Map.of(303L, catalogVariant(303L, "Replacement Medicine 20mg", true, null, null))
                );

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(0, result.getInvalidItems().size());
        assertEquals(1, result.getReplacements().size());
        ReplacementSuggestionDTO replacement = result.getReplacements().get(0);
        assertEquals(202L, replacement.getOldVariantId());
        assertEquals(303L, replacement.getNewVariantId());
        assertEquals(new BigDecimal("80.00"), replacement.getCurrentUnitPrice());
        assertEquals(new BigDecimal("80.00"), replacement.getFinalUnitPrice());
        assertEquals(new BigDecimal("4400.00"), replacement.getLineTotal());
        assertEquals(new BigDecimal("0.00"), result.getTotalPrice());
    }

    @Test
    void successorNotInActivePricelistReturnsInvalidItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(202L, 10)));
        when(catalogService.findVariantsByIdsIncludingInactive(any()))
                .thenReturn(
                        Map.of(202L, catalogVariant(202L, "Old Medicine 10mg", false, 404L, "Missing Replacement 20mg")),
                        Map.of(404L, catalogVariant(404L, "Missing Replacement 20mg", true, null, null))
                );

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(0, result.getReplacements().size());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("REPLACEMENT_NOT_IN_PRICELIST", result.getInvalidItems().get(0).getErrorCode());
    }

    @Test
    void discontinuedVariantWithoutSuccessorReturnsInvalidItem() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(202L, 10)));
        when(catalogService.findVariantsByIdsIncludingInactive(any()))
                .thenReturn(Map.of(202L, catalogVariant(202L, "Old Medicine 10mg", false, null, null)));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        assertFalse(result.isValid());
        assertEquals(0, result.getReplacements().size());
        assertEquals(1, result.getInvalidItems().size());
        assertEquals("DISCONTINUED_NO_REPLACEMENT", result.getInvalidItems().get(0).getErrorCode());
    }

    @Test
    void replacementCalculationAppliesDiscount() throws Exception {
        when(parser.parse(file)).thenReturn(List.of(new OrderDocumentItemDTO(202L, 55)));
        when(catalogService.findVariantsByIdsIncludingInactive(any()))
                .thenReturn(
                        Map.of(202L, catalogVariant(202L, "Old Medicine 10mg", false, 303L, "Replacement Medicine 20mg")),
                        Map.of(303L, catalogVariant(303L, "Replacement Medicine 20mg", true, null, null))
                );
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any()))
                .thenReturn(List.of(offer(pricelist, 303L, DiscountType.PERCENTAGE, "10.00")));

        ValidationResultDTO result = service.validateOrderDocument("buyer", file);

        ReplacementSuggestionDTO replacement = result.getReplacements().get(0);
        assertEquals(new BigDecimal("80.00"), replacement.getCurrentUnitPrice());
        assertEquals(new BigDecimal("8.00"), replacement.getDiscountAmount());
        assertEquals(new BigDecimal("10.00"), replacement.getDiscountPercentage());
        assertEquals(new BigDecimal("72.00"), replacement.getFinalUnitPrice());
        assertEquals(new BigDecimal("3960.00"), replacement.getLineTotal());
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

        PricelistItem replacementItem = new PricelistItem();
        replacementItem.setVariantId(303L);
        replacementItem.setVariantName("Replacement Medicine 20mg");
        replacementItem.setThresholds(List.of(
                threshold(1, 50, "85.00"),
                threshold(51, null, "80.00")
        ));
        pricelist.addItem(replacementItem);
        return pricelist;
    }

    private QuantityThreshold threshold(Integer quantityFrom, Integer quantityTo, String price) {
        QuantityThreshold threshold = new QuantityThreshold();
        threshold.setQuantityFrom(quantityFrom);
        threshold.setQuantityTo(quantityTo);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }

    private SpecialOffer offer(Pricelist pricelist, Long variantId, DiscountType type, String value) {
        SpecialOffer offer = new SpecialOffer();
        offer.setPricelist(pricelist);
        offer.setVariantId(variantId);
        offer.setVariantName(variantId.equals(303L) ? "Replacement Medicine 20mg" : "Medicine A 10mg");
        offer.setDiscountType(type);
        offer.setDiscountValue(new BigDecimal(value));
        offer.setStatus(SpecialOfferStatus.ACTIVE);
        return offer;
    }

    private CatalogVariantDTO catalogVariant(Long id, String name, boolean active, Long replacementVariantId, String replacementVariantName) {
        return new CatalogVariantDTO(id, name, active, replacementVariantId, replacementVariantName);
    }

    private Region region(Long id, String name, String code) {
        Region region = new Region(name, code);
        region.setId(id);
        return region;
    }
}
