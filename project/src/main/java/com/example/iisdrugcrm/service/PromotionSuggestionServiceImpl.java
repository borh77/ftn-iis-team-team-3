package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.dto.pricelist.PromotionSuggestionDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionSuggestionServiceImpl implements PromotionSuggestionService {

    private static final int MAX_SUGGESTIONS = 5;
    private static final BigDecimal LOW_CONFIDENCE_DISCOUNT = new BigDecimal("5.00");
    private static final BigDecimal NORMAL_DISCOUNT = new BigDecimal("10.00");

    private final PricelistRepository pricelistRepository;
    private final SpecialOfferRepository specialOfferRepository;

    public PromotionSuggestionServiceImpl(PricelistRepository pricelistRepository, SpecialOfferRepository specialOfferRepository) {
        this.pricelistRepository = pricelistRepository;
        this.specialOfferRepository = specialOfferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionSuggestionDTO> getSuggestions(String customerSegment) {
        if (customerSegment == null || customerSegment.isBlank()) {
            throw new IllegalArgumentException("Customer segment is required.");
        }

        String normalizedSegment = customerSegment.trim();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<Pricelist> activePricelists = pricelistRepository.findActivePricelistsByCustomerSegment(normalizedSegment, now);
        if (activePricelists.isEmpty()) {
            return List.of();
        }

        Set<Long> activelyPromotedVariantIds = new HashSet<>();
        for (SpecialOffer offer : specialOfferRepository.findActiveOffersForCustomerSegment(normalizedSegment, now)) {
            if (offer.getVariantId() != null) {
                activelyPromotedVariantIds.add(offer.getVariantId());
            }
        }

        Map<Long, Long> historicalOfferCounts = new HashMap<>();
        for (SpecialOffer offer : specialOfferRepository.findAllOffersForCustomerSegment(normalizedSegment)) {
            if (offer.getVariantId() != null) {
                historicalOfferCounts.merge(offer.getVariantId(), 1L, Long::sum);
            }
        }

        Map<Long, Candidate> candidates = new LinkedHashMap<>();
        for (Pricelist pricelist : activePricelists) {
            if (pricelist.getItems() == null) {
                continue;
            }
            for (PricelistItem item : pricelist.getItems()) {
                if (item == null || item.getVariantId() == null || activelyPromotedVariantIds.contains(item.getVariantId())) {
                    continue;
                }
                Candidate candidate = candidates.computeIfAbsent(item.getVariantId(), variantId -> new Candidate(variantId, item.getVariantName()));
                candidate.occurrences++;
                candidate.bestBasePrice = max(candidate.bestBasePrice, findBasePrice(item));
            }
        }

        return candidates.values().stream()
                .peek(candidate -> candidate.historicalOfferCount = historicalOfferCounts.getOrDefault(candidate.variantId, 0L))
                .sorted(Comparator
                        .comparingInt(Candidate::score).reversed()
                        .thenComparing(candidate -> candidate.targetName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(candidate -> candidate.variantId))
                .limit(MAX_SUGGESTIONS)
                .map(candidate -> toDto(candidate, normalizedSegment))
                .toList();
    }

    private PromotionSuggestionDTO toDto(Candidate candidate, String customerSegment) {
        PromotionSuggestionDTO dto = new PromotionSuggestionDTO();
        dto.setVariantId(candidate.variantId);
        dto.setBrandId(null);
        dto.setTargetName(candidate.targetName);
        dto.setCustomerSegment(customerSegment);
        dto.setSuggestedDiscountType(DiscountType.PERCENTAGE);
        dto.setSuggestedDiscountValue(candidate.historicalOfferCount == 0 ? NORMAL_DISCOUNT : LOW_CONFIDENCE_DISCOUNT);
        dto.setReason(reason(candidate));
        dto.setExpectedEffect("A conservative targeted discount can increase interest without bypassing existing promotion validation rules.");
        dto.setSource(candidate.historicalOfferCount == 0 ? "ACTIVE_PRICELIST_HEURISTIC" : "SPECIAL_OFFER_HISTORY");
        return dto;
    }

    private String reason(Candidate candidate) {
        if (candidate.historicalOfferCount == 0) {
            return "Variant is available in an active pricelist for this customer segment and currently has no active promotion or previous promotion history.";
        }
        return "Variant is available in an active pricelist for this customer segment, currently has no active promotion, and previous special-offer history supports a smaller targeted discount.";
    }

    private BigDecimal findBasePrice(PricelistItem item) {
        if (item.getThresholds() == null || item.getThresholds().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return item.getThresholds().stream()
                .filter(threshold -> threshold != null && threshold.getPrice() != null && threshold.getQuantityFrom() != null)
                .min(Comparator.comparing(QuantityThreshold::getQuantityFrom))
                .map(QuantityThreshold::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal max(BigDecimal first, BigDecimal second) {
        if (first == null) {
            return second == null ? BigDecimal.ZERO : second;
        }
        if (second == null) {
            return first;
        }
        return first.max(second);
    }

    private static class Candidate {
        private final Long variantId;
        private final String targetName;
        private int occurrences;
        private long historicalOfferCount;
        private BigDecimal bestBasePrice = BigDecimal.ZERO;

        private Candidate(Long variantId, String targetName) {
            this.variantId = variantId;
            this.targetName = targetName;
        }

        private int score() {
            int historyScore = historicalOfferCount == 0 ? 30 : Math.max(0, 15 - (int) historicalOfferCount * 3);
            int occurrenceScore = occurrences * 10;
            int priceScore = bestBasePrice.compareTo(new BigDecimal("1000.00")) >= 0 ? 8
                    : bestBasePrice.compareTo(new BigDecimal("500.00")) >= 0 ? 5
                    : 2;
            return historyScore + occurrenceScore + priceScore;
        }
    }
}
