package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.procurement.ProcurementOrder;
import com.example.iisdrugcrm.domain.procurement.ProcurementOrderItem;
import com.example.iisdrugcrm.domain.procurement.ProcurementOrderStatus;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementItemDTO;
import com.example.iisdrugcrm.dto.procurement.ConfirmProcurementRequestDTO;
import com.example.iisdrugcrm.dto.procurement.ProcurementOrderResponseDTO;
import com.example.iisdrugcrm.exception.InvalidProcurementConfirmationException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.ProcurementOrderRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementOrderServiceImpl implements ProcurementOrderService {

    private static final String INVALID_ITEMS_MESSAGE = "Procurement cannot be confirmed because it contains invalid items.";
    private static final String REPLACEMENTS_NOT_ACCEPTED_MESSAGE = "Procurement cannot be confirmed until all replacement suggestions are accepted.";
    private static final String VARIANT_NOT_IN_PRICELIST_MESSAGE = "Variant is not available in the active pricelist.";
    private static final String NO_ACTIVE_PRICELIST_MESSAGE = "No active pricelist found for buyer region and customer segment.";

    private final UserRepository userRepository;
    private final PricelistRepository pricelistRepository;
    private final ProcurementOrderRepository procurementOrderRepository;
    private final CatalogService catalogService;
    private final ProcurementPricingService pricingService;

    public ProcurementOrderServiceImpl(
            UserRepository userRepository,
            PricelistRepository pricelistRepository,
            ProcurementOrderRepository procurementOrderRepository,
            CatalogService catalogService,
            ProcurementPricingService pricingService
    ) {
        this.userRepository = userRepository;
        this.pricelistRepository = pricelistRepository;
        this.procurementOrderRepository = procurementOrderRepository;
        this.catalogService = catalogService;
        this.pricingService = pricingService;
    }

    @Override
    @Transactional
    public ProcurementOrderResponseDTO confirm(String username, ConfirmProcurementRequestDTO request) {
        User buyer = loadBuyer(username);
        validateBuyerProfile(buyer);
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw invalid(INVALID_ITEMS_MESSAGE);
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Pricelist pricelist = findActivePricelist(buyer, now);
        Map<Long, PricelistItem> pricelistItemsByVariantId = pricelist.getItems().stream()
                .collect(Collectors.toMap(PricelistItem::getVariantId, Function.identity(), (first, ignored) -> first));
        Map<Long, SpecialOffer> activeOffersByVariantId = pricingService.activeOffersByVariantId(pricelist.getId(), now);
        Map<Long, CatalogVariantDTO> catalogVariants = catalogVariantsFor(request);

        ProcurementOrder order = new ProcurementOrder();
        order.setBuyerId(buyer.getId());
        order.setBuyerUsername(buyer.getUsername());
        order.setBuyerDisplayName(displayName(buyer));
        order.setRegion(buyer.getBuyerRegion());
        order.setRegionName(buyer.getBuyerRegion().getName());
        order.setCustomerSegment(buyer.getCustomerSegment().trim());
        order.setPricelist(pricelist);
        order.setSourceFileName(normalizedSourceFileName(request.getSourceFileName()));
        order.setStatus(ProcurementOrderStatus.SUBMITTED);
        order.setCurrency(pricelist.getCurrency());
        order.setCreatedAt(now);
        order.setConfirmedAt(now);

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (ConfirmProcurementItemDTO requestItem : request.getItems()) {
            ProcurementOrderItem item = buildOrderItem(
                    requestItem,
                    pricelistItemsByVariantId,
                    activeOffersByVariantId,
                    catalogVariants
            );
            order.addItem(item);
            totalPrice = totalPrice.add(item.getLineTotal());
        }
        order.setTotalPrice(pricingService.scaleMoney(totalPrice));

        ProcurementOrder saved = procurementOrderRepository.save(order);
        return ProcurementOrderResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementOrderResponseDTO> listMine(String username) {
        User buyer = loadBuyer(username);
        ensureBuyer(buyer);
        return procurementOrderRepository.findAllByBuyerIdOrderByCreatedAtDesc(buyer.getId()).stream()
                .map(ProcurementOrderResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementOrderResponseDTO getMine(String username, Long id) {
        User buyer = loadBuyer(username);
        ensureBuyer(buyer);
        return procurementOrderRepository.findByIdAndBuyerId(id, buyer.getId())
                .map(ProcurementOrderResponseDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Procurement order not found."));
    }

    private ProcurementOrderItem buildOrderItem(
            ConfirmProcurementItemDTO requestItem,
            Map<Long, PricelistItem> pricelistItemsByVariantId,
            Map<Long, SpecialOffer> activeOffersByVariantId,
            Map<Long, CatalogVariantDTO> catalogVariants
    ) {
        validateRequestItemBasics(requestItem);

        CatalogVariantDTO finalVariant = catalogVariants.get(requestItem.getVariantId());
        if (finalVariant == null) {
            throw invalid(INVALID_ITEMS_MESSAGE);
        }
        if (!finalVariant.isActive()) {
            throw invalid("Variant is not available in the active pricelist.");
        }

        validateReplacementSelection(requestItem, catalogVariants);

        PricelistItem pricelistItem = pricelistItemsByVariantId.get(requestItem.getVariantId());
        if (pricelistItem == null) {
            throw invalid(VARIANT_NOT_IN_PRICELIST_MESSAGE);
        }

        ProcurementPricingService.PricedLine pricedLine = pricingService.priceLine(
                pricelistItem,
                requestItem.getRequestedQuantity(),
                activeOffersByVariantId.get(requestItem.getVariantId())
        );
        if (pricedLine == null) {
            throw invalid(INVALID_ITEMS_MESSAGE);
        }

        ProcurementOrderItem item = new ProcurementOrderItem();
        item.setOriginalVariantId(requestItem.getOriginalVariantId());
        item.setOriginalVariantName(originalVariantName(requestItem, catalogVariants));
        item.setVariantId(pricelistItem.getVariantId());
        item.setVariantName(pricelistItem.getVariantName());
        item.setRequestedQuantity(requestItem.getRequestedQuantity());
        item.setUnitPrice(pricedLine.unitPrice());
        item.setDiscountType(pricedLine.discountType());
        item.setDiscountValue(pricedLine.discountValue());
        item.setFinalUnitPrice(pricedLine.finalUnitPrice());
        item.setLineTotal(pricedLine.lineTotal());
        item.setReplacementAccepted(requestItem.isReplacementAccepted());
        return item;
    }

    private void validateRequestItemBasics(ConfirmProcurementItemDTO item) {
        if (item == null || item.getVariantId() == null || item.getRequestedQuantity() == null || item.getRequestedQuantity() <= 0) {
            throw invalid(INVALID_ITEMS_MESSAGE);
        }
        if (item.getOriginalVariantId() != null && !item.isReplacementAccepted()) {
            throw invalid(REPLACEMENTS_NOT_ACCEPTED_MESSAGE);
        }
        if (item.isReplacementAccepted() && item.getOriginalVariantId() == null) {
            throw invalid(REPLACEMENTS_NOT_ACCEPTED_MESSAGE);
        }
    }

    private void validateReplacementSelection(ConfirmProcurementItemDTO item, Map<Long, CatalogVariantDTO> catalogVariants) {
        if (!item.isReplacementAccepted()) {
            return;
        }

        CatalogVariantDTO originalVariant = catalogVariants.get(item.getOriginalVariantId());
        if (originalVariant == null || originalVariant.isActive()) {
            throw invalid(REPLACEMENTS_NOT_ACCEPTED_MESSAGE);
        }
        if (originalVariant.getReplacementVariantId() == null || !originalVariant.getReplacementVariantId().equals(item.getVariantId())) {
            throw invalid(REPLACEMENTS_NOT_ACCEPTED_MESSAGE);
        }
    }

    private Map<Long, CatalogVariantDTO> catalogVariantsFor(ConfirmProcurementRequestDTO request) {
        Set<Long> variantIds = new LinkedHashSet<>();
        for (ConfirmProcurementItemDTO item : request.getItems()) {
            if (item != null) {
                if (item.getVariantId() != null) {
                    variantIds.add(item.getVariantId());
                }
                if (item.getOriginalVariantId() != null) {
                    variantIds.add(item.getOriginalVariantId());
                }
            }
        }
        return catalogService.findVariantsByIdsIncludingInactive(variantIds);
    }

    private User loadBuyer(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> invalid("User not found"));
    }

    private void validateBuyerProfile(User buyer) {
        ensureBuyer(buyer);
        if (buyer.getBuyerRegion() == null || buyer.getCustomerSegment() == null || buyer.getCustomerSegment().isBlank()) {
            throw invalid("Buyer region and customer segment are required for procurement confirmation.");
        }
    }

    private void ensureBuyer(User buyer) {
        if (buyer.getRole() != UserRole.ROLE_BUYER) {
            throw invalid("Only buyers can confirm procurement orders.");
        }
    }

    private Pricelist findActivePricelist(User buyer, OffsetDateTime now) {
        List<Pricelist> matches = pricelistRepository.findActiveBuyerPricelists(
                buyer.getBuyerRegion().getId(),
                buyer.getCustomerSegment().trim(),
                now
        );
        if (matches.isEmpty() || matches.get(0).getStatus() != PricelistStatus.ACTIVE) {
            throw invalid(NO_ACTIVE_PRICELIST_MESSAGE);
        }
        return matches.get(0);
    }

    private String displayName(User buyer) {
        String firstName = buyer.getFirstName() == null ? "" : buyer.getFirstName().trim();
        String lastName = buyer.getLastName() == null ? "" : buyer.getLastName().trim();
        String displayName = (firstName + " " + lastName).trim();
        return displayName.isBlank() ? buyer.getUsername() : displayName;
    }

    private String normalizedSourceFileName(String sourceFileName) {
        if (sourceFileName == null || sourceFileName.isBlank()) {
            return null;
        }
        return sourceFileName.trim();
    }

    private String originalVariantName(ConfirmProcurementItemDTO item, Map<Long, CatalogVariantDTO> catalogVariants) {
        if (!item.isReplacementAccepted()) {
            return null;
        }
        if (item.getOriginalVariantName() != null && !item.getOriginalVariantName().isBlank()) {
            return item.getOriginalVariantName().trim();
        }
        CatalogVariantDTO originalVariant = catalogVariants.get(item.getOriginalVariantId());
        return originalVariant == null ? null : originalVariant.getName();
    }

    private InvalidProcurementConfirmationException invalid(String message) {
        return new InvalidProcurementConfirmationException(message);
    }
}
