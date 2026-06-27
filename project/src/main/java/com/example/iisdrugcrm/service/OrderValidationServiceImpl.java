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
import java.util.List;
import java.util.Map;
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

        Map<Long, PricelistItem> pricelistItemsByVariantId = pricelist.getItems().stream()
                .collect(Collectors.toMap(PricelistItem::getVariantId, Function.identity(), (first, ignored) -> first));
        Map<Long, CatalogVariantDTO> activeVariants = catalogService.findActiveVariantsByIds(
                orderItems.stream()
                        .map(OrderDocumentItemDTO::getVariantId)
                        .filter(variantId -> variantId != null)
                        .toList()
        );
        Map<Long, SpecialOffer> activeOffersByVariantId = specialOfferRepository
                .findActiveOffersForPricelist(pricelist.getId(), now)
                .stream()
                .collect(Collectors.toMap(SpecialOffer::getVariantId, Function.identity(), (first, ignored) -> first));

        ValidationResultDTO result = new ValidationResultDTO();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderDocumentItemDTO orderItem : orderItems) {
            InvalidOrderItemDTO invalidItem = validateItemBasics(orderItem, activeVariants, pricelistItemsByVariantId);
            if (invalidItem != null) {
                result.getInvalidItems().add(invalidItem);
                continue;
            }

            PricelistItem pricelistItem = pricelistItemsByVariantId.get(orderItem.getVariantId());
            QuantityThreshold threshold = findMatchingThreshold(pricelistItem, orderItem.getRequestedQuantity());
            if (threshold == null) {
                result.getInvalidItems().add(new InvalidOrderItemDTO(
                        orderItem.getVariantId(),
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
        result.setValid(result.getInvalidItems().isEmpty());
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
            Map<Long, CatalogVariantDTO> activeVariants,
            Map<Long, PricelistItem> pricelistItemsByVariantId
    ) {
        if (item.getVariantId() == null) {
            return new InvalidOrderItemDTO(null, item.getRequestedQuantity(), "MISSING_VARIANT_ID", "Variant id is required.");
        }
        if (item.getRequestedQuantity() == null || item.getRequestedQuantity() <= 0) {
            return new InvalidOrderItemDTO(item.getVariantId(), item.getRequestedQuantity(), "INVALID_QUANTITY", "Requested quantity must be positive.");
        }
        if (!activeVariants.containsKey(item.getVariantId())) {
            return new InvalidOrderItemDTO(item.getVariantId(), item.getRequestedQuantity(), "VARIANT_NOT_FOUND", "Variant does not exist or is not active.");
        }
        if (!pricelistItemsByVariantId.containsKey(item.getVariantId())) {
            return new InvalidOrderItemDTO(item.getVariantId(), item.getRequestedQuantity(), "VARIANT_NOT_IN_PRICELIST", "Variant is not available in the active pricelist.");
        }
        return null;
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
}
