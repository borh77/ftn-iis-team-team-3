package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.DiscountType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.domain.pricelist.SpecialOfferStatus;
import com.example.iisdrugcrm.dto.pricelist.CreateSpecialOfferDTO;
import com.example.iisdrugcrm.dto.pricelist.SpecialOfferResponseDTO;
import com.example.iisdrugcrm.exception.PricelistNotFoundException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecialOfferServiceImpl implements SpecialOfferService {

    private final SpecialOfferRepository specialOfferRepository;
    private final PricelistRepository pricelistRepository;
    private final PricelistAccessService accessService;

    public SpecialOfferServiceImpl(SpecialOfferRepository specialOfferRepository, PricelistRepository pricelistRepository, PricelistAccessService accessService) {
        this.specialOfferRepository = specialOfferRepository;
        this.pricelistRepository = pricelistRepository;
        this.accessService = accessService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecialOfferResponseDTO createOffer(CreateSpecialOfferDTO dto, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(dto.getPricelistId())
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        accessService.validateOwnerOrTeamMember(pricelist, currentUserId);
        if (pricelist.getStatus() == PricelistStatus.ARCHIVED) {
            throw new IllegalArgumentException("Archived pricelists cannot receive offers.");
        }

        PricelistItem item = findPricelistItemOrThrow(pricelist, dto.getVariantId());

        validateDiscount(dto.getDiscountType(), dto.getDiscountValue());
        OffsetDateTime start = dto.getStartDate().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime end = dto.getEndDate().withOffsetSameInstant(ZoneOffset.UTC);
        validateOfferPeriod(pricelist, start, end);

        SpecialOffer offer = new SpecialOffer();
        offer.setPricelist(pricelist);
        offer.setVariantId(item.getVariantId());
        offer.setVariantName(item.getVariantName());
        offer.setDiscountType(dto.getDiscountType());
        offer.setDiscountValue(dto.getDiscountValue());
        offer.setStartDate(start);
        offer.setEndDate(end);
        offer.setCreatedBy(currentUserId);
        offer.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        return SpecialOfferResponseDTO.fromEntity(specialOfferRepository.save(offer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialOfferResponseDTO> listOffersForPricelist(Long pricelistId, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(pricelistId)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        accessService.validateOwnerOrTeamMember(pricelist, currentUserId);
        return specialOfferRepository.findAllByPricelistIdOrderByIdDesc(pricelistId).stream()
                .map(SpecialOfferResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecialOfferResponseDTO activateOffer(Long id, Long currentUserId) {
        SpecialOffer offer = specialOfferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special offer not found"));
        accessService.validateOwnerOnly(offer.getPricelist(), currentUserId);
        validateOfferCanBeActivated(offer);
        offer.activate();
        return SpecialOfferResponseDTO.fromEntity(specialOfferRepository.save(offer));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecialOfferResponseDTO archiveOffer(Long id, Long currentUserId) {
        SpecialOffer offer = specialOfferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special offer not found"));
        accessService.validateOwnerOnly(offer.getPricelist(), currentUserId);
        offer.archive();
        return SpecialOfferResponseDTO.fromEntity(specialOfferRepository.save(offer));
    }

    private void validateOfferCanBeActivated(SpecialOffer offer) {
        if (offer.getStatus() != SpecialOfferStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft offers can be activated.");
        }
        if (offer.getPricelist().getStatus() == PricelistStatus.ARCHIVED) {
            throw new IllegalArgumentException("Archived pricelists cannot receive offers.");
        }
        validateDiscount(offer.getDiscountType(), offer.getDiscountValue());
        validateOfferPeriod(offer.getPricelist(), offer.getStartDate(), offer.getEndDate());
        PricelistItem item = findPricelistItemOrThrow(offer.getPricelist(), offer.getVariantId());
        BigDecimal basePrice = findBasePriceOrThrow(item);
        if (offer.getDiscountType() == DiscountType.FIXED_AMOUNT && offer.getDiscountValue().compareTo(basePrice) > 0) {
            throw new IllegalArgumentException("Discount cannot reduce price below zero.");
        }
    }

    private void validateDiscount(DiscountType type, BigDecimal value) {
        if (type == null || value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value is invalid.");
        }
        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount value is invalid.");
        }
    }

    private void validateOfferPeriod(Pricelist pricelist, OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Offer period must be inside the pricelist period.");
        }
        if (start.isBefore(pricelist.getPeriodStart()) || end.isAfter(pricelist.getPeriodEnd())) {
            throw new IllegalArgumentException("Offer period must be inside the pricelist period.");
        }
    }

    private PricelistItem findPricelistItemOrThrow(Pricelist pricelist, Long variantId) {
        if (pricelist.getItems() == null || variantId == null) {
            throw new IllegalArgumentException("Selected variant is not part of this pricelist.");
        }
        return pricelist.getItems().stream()
                .filter(candidate -> candidate != null && variantId.equals(candidate.getVariantId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selected variant is not part of this pricelist."));
    }

    private BigDecimal findBasePriceOrThrow(PricelistItem item) {
        if (item.getThresholds() == null || item.getThresholds().isEmpty()) {
            throw new IllegalArgumentException("Base price could not be determined.");
        }
        QuantityThreshold baseThreshold = item.getThresholds().stream()
                .filter(threshold -> threshold != null && threshold.getQuantityFrom() != null)
                .sorted(Comparator.comparing(QuantityThreshold::getQuantityFrom))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Base price could not be determined."));
        BigDecimal basePrice = baseThreshold.getPrice();
        if (basePrice == null) {
            throw new IllegalArgumentException("Base price could not be determined.");
        }
        return basePrice;
    }
}
