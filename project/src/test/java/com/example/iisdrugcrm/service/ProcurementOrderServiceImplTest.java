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
import com.example.iisdrugcrm.domain.procurement.ProcurementOrder;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementItemDTO;
import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementRequestDTO;
import com.example.iisdrugcrm.dto.procurement.ProcurementOrderResponseDTO;
import com.example.iisdrugcrm.exception.InvalidProcurementConfirmationException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.ProcurementOrderRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcurementOrderServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PricelistRepository pricelistRepository;

    @Mock
    private ProcurementOrderRepository procurementOrderRepository;

    @Mock
    private SpecialOfferRepository specialOfferRepository;

    @Mock
    private CatalogService catalogService;

    private ProcurementOrderServiceImpl service;
    private User buyer;
    private Region region;
    private Pricelist pricelist;

    @BeforeEach
    void setUp() {
        ProcurementPricingService pricingService = new ProcurementPricingService(specialOfferRepository);
        service = new ProcurementOrderServiceImpl(userRepository, pricelistRepository, procurementOrderRepository, catalogService, pricingService);
        region = region(1L, "Srbija", "RS");
        buyer = buyer(7L, "buyer", UserRole.ROLE_BUYER, region, "Pharmacy chains");
        pricelist = pricelist(10L, region, "Pharmacy chains");

        lenient().when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        lenient().when(pricelistRepository.findActiveBuyerPricelists(eq(1L), eq("Pharmacy chains"), any()))
                .thenReturn(List.of(pricelist));
        lenient().when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any())).thenReturn(List.of());
        lenient().when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(
                        101L, new CatalogVariantDTO(101L, "Medicine A 10mg", true),
                        102L, new CatalogVariantDTO(102L, "Medicine B 20mg", true)
                ));
        lenient().when(procurementOrderRepository.save(any(ProcurementOrder.class))).thenAnswer(invocation -> {
            ProcurementOrder order = invocation.getArgument(0);
            order.setId(500L);
            return order;
        });
    }

    @Test
    void validProcurementIsSavedWithBuyerPricelistAndRecomputedTotal() {
        ProcurementOrderResponseDTO response = service.confirm("buyer", request(item(101L, 55)));

        assertEquals(500L, response.getId());
        assertEquals("buyer", response.getBuyerUsername());
        assertEquals("Srbija", response.getRegionName());
        assertEquals("Pharmacy chains", response.getCustomerSegment());
        assertEquals(10L, response.getPricelistId());
        assertEquals(new BigDecimal("4950.00"), response.getTotalPrice());

        ArgumentCaptor<ProcurementOrder> captor = ArgumentCaptor.forClass(ProcurementOrder.class);
        verify(procurementOrderRepository).save(captor.capture());
        ProcurementOrder saved = captor.getValue();
        assertEquals(7L, saved.getBuyerId());
        assertEquals("RSD", saved.getCurrency());
        assertEquals(new BigDecimal("90.00"), saved.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("4950.00"), saved.getItems().get(0).getLineTotal());
    }

    @Test
    void itemPricesAreRecomputedWithActiveOffer() {
        when(specialOfferRepository.findActiveOffersForPricelist(eq(10L), any()))
                .thenReturn(List.of(offer(101L, DiscountType.PERCENTAGE, "10.00")));

        ProcurementOrderResponseDTO response = service.confirm("buyer", request(item(101L, 10)));

        assertEquals(new BigDecimal("900.00"), response.getItems().get(0).getLineTotal());
        assertEquals(new BigDecimal("100.00"), response.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("90.00"), response.getItems().get(0).getFinalUnitPrice());
        assertEquals(DiscountType.PERCENTAGE, response.getItems().get(0).getDiscountType());
    }

    @Test
    void acceptedReplacementSavesOriginalAndReplacementVariantIds() {
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(
                        104L, new CatalogVariantDTO(104L, "Old Medicine", false, 102L, "Medicine B 20mg"),
                        102L, new CatalogVariantDTO(102L, "Medicine B 20mg", true)
                ));

        ConfirmProcurementItemDTO requestItem = item(102L, 3);
        requestItem.setOriginalVariantId(104L);
        requestItem.setOriginalVariantName("Old Medicine");
        requestItem.setReplacementAccepted(true);

        ProcurementOrderResponseDTO response = service.confirm("buyer", request(requestItem));

        assertEquals(104L, response.getItems().get(0).getOriginalVariantId());
        assertEquals(102L, response.getItems().get(0).getVariantId());
        assertTrue(response.getItems().get(0).isReplacementAccepted());
    }

    @Test
    void invalidQuantityCannotBeConfirmed() {
        InvalidProcurementConfirmationException exception = assertThrows(
                InvalidProcurementConfirmationException.class,
                () -> service.confirm("buyer", request(item(101L, 0)))
        );

        assertEquals("Procurement cannot be confirmed because it contains invalid items.", exception.getMessage());
        verify(procurementOrderRepository, never()).save(any());
    }

    @Test
    void discontinuedVariantCannotBeConfirmedDirectly() {
        when(catalogService.findVariantsByIdsIncludingInactive(anyCollection()))
                .thenReturn(Map.of(104L, new CatalogVariantDTO(104L, "Old Medicine", false, 102L, "Medicine B 20mg")));

        InvalidProcurementConfirmationException exception = assertThrows(
                InvalidProcurementConfirmationException.class,
                () -> service.confirm("buyer", request(item(104L, 2)))
        );

        assertEquals("Variant is not available in the active pricelist.", exception.getMessage());
        verify(procurementOrderRepository, never()).save(any());
    }

    @Test
    void nonBuyerCannotConfirm() {
        User creator = buyer(8L, "creator", UserRole.ROLE_PRICELIST_CREATOR, region, "Pharmacy chains");
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));

        InvalidProcurementConfirmationException exception = assertThrows(
                InvalidProcurementConfirmationException.class,
                () -> service.confirm("creator", request(item(101L, 2)))
        );

        assertEquals("Only buyers can confirm procurement orders.", exception.getMessage());
        verify(procurementOrderRepository, never()).save(any());
    }

    @Test
    void buyerCanOnlyListOwnProcurements() {
        ProcurementOrder order = new ProcurementOrder();
        order.setId(500L);
        order.setBuyerId(7L);
        order.setBuyerUsername("buyer");
        order.setBuyerDisplayName("buyer");
        order.setRegionName("Srbija");
        order.setCustomerSegment("Pharmacy chains");
        order.setPricelist(pricelist);
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setCurrency("RSD");
        order.setCreatedAt(OffsetDateTime.now());
        order.setConfirmedAt(OffsetDateTime.now());
        when(procurementOrderRepository.findAllByBuyerIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(order));

        List<ProcurementOrderResponseDTO> result = service.listMine("buyer");

        assertEquals(1, result.size());
        assertEquals(500L, result.get(0).getId());
        verify(procurementOrderRepository).findAllByBuyerIdOrderByCreatedAtDesc(7L);
    }

    private ConfirmProcurementRequestDTO request(ConfirmProcurementItemDTO item) {
        ConfirmProcurementRequestDTO request = new ConfirmProcurementRequestDTO();
        request.setSourceFileName("order.csv");
        request.setItems(List.of(item));
        return request;
    }

    private ConfirmProcurementItemDTO item(Long variantId, Integer quantity) {
        ConfirmProcurementItemDTO item = new ConfirmProcurementItemDTO();
        item.setVariantId(variantId);
        item.setRequestedQuantity(quantity);
        return item;
    }

    private User buyer(Long id, String username, UserRole role, Region region, String segment) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setBuyerRegion(region);
        user.setCustomerSegment(segment);
        return user;
    }

    private Region region(Long id, String name, String code) {
        Region region = new Region(name, code);
        region.setId(id);
        return region;
    }

    private Pricelist pricelist(Long id, Region region, String segment) {
        Pricelist pricelist = new Pricelist();
        pricelist.setId(id);
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(segment);
        pricelist.setCurrency("RSD");
        pricelist.setStatus(PricelistStatus.ACTIVE);
        pricelist.addItem(pricelistItem(101L, "Medicine A 10mg"));
        pricelist.addItem(pricelistItem(102L, "Medicine B 20mg"));
        return pricelist;
    }

    private PricelistItem pricelistItem(Long variantId, String variantName) {
        PricelistItem item = new PricelistItem();
        item.setVariantId(variantId);
        item.setVariantName(variantName);
        item.setThresholds(List.of(threshold(1, 50, "100.00"), threshold(51, null, "90.00")));
        return item;
    }

    private QuantityThreshold threshold(Integer from, Integer to, String price) {
        QuantityThreshold threshold = new QuantityThreshold();
        threshold.setQuantityFrom(from);
        threshold.setQuantityTo(to);
        threshold.setPrice(new BigDecimal(price));
        return threshold;
    }

    private SpecialOffer offer(Long variantId, DiscountType discountType, String discountValue) {
        SpecialOffer offer = new SpecialOffer();
        offer.setVariantId(variantId);
        offer.setDiscountType(discountType);
        offer.setDiscountValue(new BigDecimal(discountValue));
        offer.setStatus(SpecialOfferStatus.ACTIVE);
        return offer;
    }
}
