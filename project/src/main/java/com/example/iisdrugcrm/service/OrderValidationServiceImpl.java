package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.dto.order.InvalidOrderItemDTO;
import com.example.iisdrugcrm.dto.order.OrderDocumentItemDTO;
import com.example.iisdrugcrm.dto.order.ReplacementSuggestionDTO;
import com.example.iisdrugcrm.dto.order.ValidatedOrderItemDTO;
import com.example.iisdrugcrm.dto.order.ValidationResultDTO;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import com.example.iisdrugcrm.service.order.OrderDocumentParser;
import com.example.iisdrugcrm.service.order.OrderDocumentParserResolver;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderValidationServiceImpl implements OrderValidationService {

    private final UserRepository userRepository;
    private final PricelistRepository pricelistRepository;
    private final SpecialOfferRepository specialOfferRepository;
    private final CatalogService catalogService;
    private final OrderDocumentParserResolver parserResolver;

    public OrderValidationServiceImpl(
            UserRepository userRepository,
            PricelistRepository pricelistRepository,
            SpecialOfferRepository specialOfferRepository,
            CatalogService catalogService,
            OrderDocumentParserResolver parserResolver
    ) {
        this.userRepository = userRepository;
        this.pricelistRepository = pricelistRepository;
        this.specialOfferRepository = specialOfferRepository;
        this.catalogService = catalogService;
        this.parserResolver = parserResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResultDTO validateOrderDocument(String username, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Order document is empty");
        }

        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        validateBuyerProfile(buyer);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Pricelist pricelist = findActivePricelist(buyer, now);
        List<OrderDocumentItemDTO> orderItems = parse(file);
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order document does not contain any items");
        }
        ValidationResultDTO result = new ValidationResultDTO();
        List<OrderDocumentItemDTO> resolvedOrderItems = resolveVariantIdentifiers(orderItems, result);

        Map<Long, PricelistItem> pricelistItemsByVariantId = pricelist.getItems().stream()
                .collect(Collectors.toMap(PricelistItem::getVariantId, Function.identity(), (first, ignored) -> first));
        Set<Long> requestedVariantIds = resolvedOrderItems.stream()
                .map(OrderDocumentItemDTO::getVariantId)
                .filter(variantId -> variantId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, CatalogVariantDTO> catalogVariants = new LinkedHashMap<>(catalogService.findVariantsByIdsIncludingInactive(requestedVariantIds));
        Set<Long> replacementVariantIds = catalogVariants.values().stream()
                .map(CatalogVariantDTO::getReplacementVariantId)
                .filter(replacementVariantId -> replacementVariantId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!replacementVariantIds.isEmpty()) {
            Map<Long, CatalogVariantDTO> replacementVariants = catalogService.findVariantsByIdsIncludingInactive(replacementVariantIds);
            catalogVariants.putAll(replacementVariants);
        }
        Map<Long, SpecialOffer> activeOffersByVariantId = specialOfferRepository
                .findActiveOffersForPricelist(pricelist.getId(), now)
                .stream()
                .collect(Collectors.toMap(SpecialOffer::getVariantId, Function.identity(), (first, ignored) -> first));

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderDocumentItemDTO orderItem : resolvedOrderItems) {
            InvalidOrderItemDTO invalidItem = validateItemBasics(orderItem, catalogVariants);
            if (invalidItem != null) {
                result.getInvalidItems().add(invalidItem);
                continue;
            }

            CatalogVariantDTO catalogVariant = catalogVariants.get(orderItem.getVariantId());
            if (!catalogVariant.isActive()) {
                ReplacementResolution replacementResolution = resolveReplacementSuggestion(
                        orderItem,
                        catalogVariant,
                        catalogVariants,
                        pricelistItemsByVariantId,
                        activeOffersByVariantId
                );
                if (replacementResolution.suggestion() != null) {
                    result.getReplacements().add(replacementResolution.suggestion());
                } else {
                    result.getInvalidItems().add(replacementResolution.invalidItem());
                }
                continue;
            }

            if (!pricelistItemsByVariantId.containsKey(orderItem.getVariantId())) {
                result.getInvalidItems().add(new InvalidOrderItemDTO(
                        orderItem.getVariantId(),
                        orderItem.getVariantName(),
                        orderItem.getProductName(),
                        orderItem.getForm(),
                        orderItem.getDosage(),
                        orderItem.getRequestedQuantity(),
                        "VARIANT_NOT_IN_PRICELIST",
                        "Variant is not available in the active pricelist."
                ));
                continue;
            }

            PricelistItem pricelistItem = pricelistItemsByVariantId.get(orderItem.getVariantId());
            QuantityThreshold threshold = findMatchingThreshold(pricelistItem, orderItem.getRequestedQuantity());
            if (threshold == null) {
                result.getInvalidItems().add(new InvalidOrderItemDTO(
                        orderItem.getVariantId(),
                        orderItem.getVariantName(),
                        orderItem.getProductName(),
                        orderItem.getForm(),
                        orderItem.getDosage(),
                        orderItem.getRequestedQuantity(),
                        "NO_QUANTITY_THRESHOLD",
                        "No quantity threshold matches the requested quantity."
                ));
                continue;
            }

            ValidatedOrderItemDTO validItem = buildValidItem(orderItem, pricelistItem, threshold, activeOffersByVariantId.get(orderItem.getVariantId()));
            result.getValidatedItems().add(validItem);
            totalPrice = totalPrice.add(validItem.getLineTotal());
        }

        result.setTotalPrice(scaleMoney(totalPrice));
        result.setValid(result.getInvalidItems().isEmpty() && result.getReplacements().isEmpty());
        return result;
    }

    private void validateBuyerProfile(User buyer) {
        if (buyer.getRole() != UserRole.ROLE_BUYER) {
            throw new IllegalArgumentException("Only buyers can validate order documents");
        }
        if (buyer.getBuyerRegion() == null || buyer.getCustomerSegment() == null || buyer.getCustomerSegment().isBlank()) {
            throw new IllegalArgumentException("Buyer region and customer segment are required for order validation");
        }
    }

    private Pricelist findActivePricelist(User buyer, OffsetDateTime now) {
        List<Pricelist> matches = pricelistRepository.findActiveBuyerPricelists(
                buyer.getBuyerRegion().getId(),
                buyer.getCustomerSegment().trim(),
                now
        );
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No active pricelist found for buyer region and customer segment");
        }
        Pricelist pricelist = matches.get(0);
        if (pricelist.getStatus() != PricelistStatus.ACTIVE) {
            throw new IllegalArgumentException("No active pricelist found for buyer region and customer segment");
        }
        return pricelist;
    }

    private List<OrderDocumentItemDTO> parse(MultipartFile file) {
        OrderDocumentParser parser = parserResolver.resolve(file);
        try {
            return parser.parse(file);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read order document");
        }
    }

    private InvalidOrderItemDTO validateItemBasics(
            OrderDocumentItemDTO item,
            Map<Long, CatalogVariantDTO> catalogVariants
    ) {
        if (item.getVariantId() == null) {
            return invalidItem(item, "MISSING_VARIANT_IDENTIFIER", "Variant id, variant name, or product/form/dosage is required.");
        }
        if (item.getRequestedQuantity() == null || item.getRequestedQuantity() <= 0) {
            return invalidItem(item, "INVALID_QUANTITY", "Requested quantity must be positive.");
        }
        if (!catalogVariants.containsKey(item.getVariantId())) {
            return invalidItem(item, "VARIANT_NOT_FOUND", "Variant was not found in the catalog.");
        }
        return null;
    }

    private List<OrderDocumentItemDTO> resolveVariantIdentifiers(List<OrderDocumentItemDTO> orderItems, ValidationResultDTO result) {
        return orderItems.stream()
                .map(item -> resolveVariantIdentifier(item, result))
                .filter(Objects::nonNull)
                .toList();
    }

    private OrderDocumentItemDTO resolveVariantIdentifier(OrderDocumentItemDTO item, ValidationResultDTO result) {
        if (item.getVariantId() != null) {
            return item;
        }

        if (hasText(item.getVariantName())) {
            List<CatalogVariantDTO> matches = catalogService.findVariantsByDisplayNameIncludingInactive(item.getVariantName());
            return applyVariantResolution(item, matches, result);
        }

        if (hasText(item.getProductName()) && hasText(item.getForm()) && hasText(item.getDosage())) {
            List<CatalogVariantDTO> matches = catalogService.findVariantsByProductFormDosageIncludingInactive(
                    item.getProductName(),
                    item.getForm(),
                    item.getDosage()
            );
            return applyVariantResolution(item, matches, result);
        }

        result.getInvalidItems().add(invalidItem(item, "MISSING_VARIANT_IDENTIFIER", "Variant id, variant name, or product/form/dosage is required."));
        return null;
    }

    private OrderDocumentItemDTO applyVariantResolution(
            OrderDocumentItemDTO item,
            List<CatalogVariantDTO> matches,
            ValidationResultDTO result
    ) {
        if (matches.isEmpty()) {
            result.getInvalidItems().add(invalidItem(item, "VARIANT_NOT_FOUND", "Variant was not found in the catalog."));
            return null;
        }
        if (matches.size() > 1) {
            result.getInvalidItems().add(invalidItem(
                    item,
                    "AMBIGUOUS_VARIANT_NAME",
                    "More than one variant matches this name. Please use variant ID or provide product, form, and dosage."
            ));
            return null;
        }

        item.setVariantId(matches.get(0).getId());
        if (!hasText(item.getVariantName())) {
            item.setVariantName(matches.get(0).getName());
        }
        return item;
    }

    private ReplacementResolution resolveReplacementSuggestion(
            OrderDocumentItemDTO orderItem,
            CatalogVariantDTO oldVariant,
            Map<Long, CatalogVariantDTO> catalogVariants,
            Map<Long, PricelistItem> pricelistItemsByVariantId,
            Map<Long, SpecialOffer> activeOffersByVariantId
    ) {
        if (oldVariant.getReplacementVariantId() == null) {
            return ReplacementResolution.invalid(new InvalidOrderItemDTO(
                    orderItem.getVariantId(),
                    orderItem.getVariantName(),
                    orderItem.getProductName(),
                    orderItem.getForm(),
                    orderItem.getDosage(),
                    orderItem.getRequestedQuantity(),
                    "DISCONTINUED_NO_REPLACEMENT",
                    "Medicine is discontinued and has no available replacement in the active pricelist."
            ));
        }

        CatalogVariantDTO replacementVariant = catalogVariants.get(oldVariant.getReplacementVariantId());
        if (replacementVariant == null || !replacementVariant.isActive()) {
            return ReplacementResolution.invalid(new InvalidOrderItemDTO(
                    orderItem.getVariantId(),
                    orderItem.getVariantName(),
                    orderItem.getProductName(),
                    orderItem.getForm(),
                    orderItem.getDosage(),
                    orderItem.getRequestedQuantity(),
                    "REPLACEMENT_NOT_ACTIVE",
                    "Medicine is discontinued and its replacement is not active."
            ));
        }

        PricelistItem replacementItem = pricelistItemsByVariantId.get(replacementVariant.getId());
        if (replacementItem == null) {
            return ReplacementResolution.invalid(new InvalidOrderItemDTO(
                    orderItem.getVariantId(),
                    orderItem.getVariantName(),
                    orderItem.getProductName(),
                    orderItem.getForm(),
                    orderItem.getDosage(),
                    orderItem.getRequestedQuantity(),
                    "REPLACEMENT_NOT_IN_PRICELIST",
                    "Medicine is discontinued and has no available replacement in the active pricelist."
            ));
        }

        QuantityThreshold threshold = findMatchingThreshold(replacementItem, orderItem.getRequestedQuantity());
        if (threshold == null) {
            return ReplacementResolution.invalid(new InvalidOrderItemDTO(
                    orderItem.getVariantId(),
                    orderItem.getVariantName(),
                    orderItem.getProductName(),
                    orderItem.getForm(),
                    orderItem.getDosage(),
                    orderItem.getRequestedQuantity(),
                    "REPLACEMENT_NO_QUANTITY_THRESHOLD",
                    "Replacement medicine has no quantity threshold for the requested quantity."
            ));
        }

        return ReplacementResolution.suggestion(buildReplacementSuggestion(
                orderItem,
                oldVariant,
                replacementItem,
                threshold,
                activeOffersByVariantId.get(replacementVariant.getId())
        ));
    }

    private QuantityThreshold findMatchingThreshold(PricelistItem item, Integer requestedQuantity) {
        return item.getThresholds().stream()
                .sorted(Comparator.comparing(QuantityThreshold::getQuantityFrom))
                .filter(threshold -> threshold.getQuantityFrom() <= requestedQuantity)
                .filter(threshold -> threshold.getQuantityTo() == null || requestedQuantity <= threshold.getQuantityTo())
                .findFirst()
                .orElse(null);
    }

    private ValidatedOrderItemDTO buildValidItem(
            OrderDocumentItemDTO orderItem,
            PricelistItem pricelistItem,
            QuantityThreshold threshold,
            SpecialOffer activeOffer
    ) {
        BigDecimal unitPrice = scaleMoney(threshold.getPrice());
        BigDecimal finalUnitPrice = applyDiscount(unitPrice, activeOffer);
        BigDecimal lineTotal = scaleMoney(finalUnitPrice.multiply(BigDecimal.valueOf(orderItem.getRequestedQuantity())));

        ValidatedOrderItemDTO dto = new ValidatedOrderItemDTO();
        dto.setVariantId(orderItem.getVariantId());
        dto.setVariantName(pricelistItem.getVariantName());
        dto.setRequestedQuantity(orderItem.getRequestedQuantity());
        dto.setUnitPrice(unitPrice);
        if (activeOffer != null) {
            dto.setDiscountType(activeOffer.getDiscountType());
            dto.setDiscountValue(scaleMoney(activeOffer.getDiscountValue()));
        }
        dto.setFinalUnitPrice(finalUnitPrice);
        dto.setLineTotal(lineTotal);
        return dto;
    }

    private ReplacementSuggestionDTO buildReplacementSuggestion(
            OrderDocumentItemDTO orderItem,
            CatalogVariantDTO oldVariant,
            PricelistItem replacementItem,
            QuantityThreshold threshold,
            SpecialOffer activeOffer
    ) {
        BigDecimal currentUnitPrice = scaleMoney(threshold.getPrice());
        BigDecimal finalUnitPrice = applyDiscount(currentUnitPrice, activeOffer);
        BigDecimal lineTotal = scaleMoney(finalUnitPrice.multiply(BigDecimal.valueOf(orderItem.getRequestedQuantity())));

        ReplacementSuggestionDTO dto = new ReplacementSuggestionDTO();
        dto.setOldVariantId(orderItem.getVariantId());
        dto.setOldVariantName(oldVariant.getName());
        dto.setNewVariantId(replacementItem.getVariantId());
        dto.setNewVariantName(replacementItem.getVariantName());
        dto.setRequestedQuantity(orderItem.getRequestedQuantity());
        dto.setCurrentUnitPrice(currentUnitPrice);
        if (activeOffer != null) {
            if (activeOffer.getDiscountType() == DiscountType.PERCENTAGE) {
                dto.setDiscountPercentage(scaleMoney(activeOffer.getDiscountValue()));
            }
            dto.setDiscountAmount(scaleMoney(currentUnitPrice.subtract(finalUnitPrice)));
        }
        dto.setFinalUnitPrice(finalUnitPrice);
        dto.setLineTotal(lineTotal);
        dto.setMessage("Requested medicine is discontinued. Suggested replacement is priced from the active pricelist.");
        return dto;
    }

    private BigDecimal applyDiscount(BigDecimal unitPrice, SpecialOffer offer) {
        if (offer == null) {
            return unitPrice;
        }
        BigDecimal discounted;
        if (offer.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal discount = unitPrice.multiply(offer.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            discounted = unitPrice.subtract(discount);
        } else {
            discounted = unitPrice.subtract(offer.getDiscountValue());
        }
        return scaleMoney(discounted.max(BigDecimal.ZERO));
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private InvalidOrderItemDTO invalidItem(OrderDocumentItemDTO item, String errorCode, String message) {
        return new InvalidOrderItemDTO(
                item.getVariantId(),
                item.getVariantName(),
                item.getProductName(),
                item.getForm(),
                item.getDosage(),
                item.getRequestedQuantity(),
                errorCode,
                message
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ReplacementResolution(ReplacementSuggestionDTO suggestion, InvalidOrderItemDTO invalidItem) {

        static ReplacementResolution suggestion(ReplacementSuggestionDTO suggestion) {
            return new ReplacementResolution(suggestion, null);
        }

        static ReplacementResolution invalid(InvalidOrderItemDTO invalidItem) {
            return new ReplacementResolution(null, invalidItem);
        }
    }
}
