package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.User;
import com.example.iisdrugcrm.domain.UserRole;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.SpecialOffer;
import com.example.iisdrugcrm.dto.pricelist.BuyerCatalogDTO;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.SpecialOfferRepository;
import com.example.iisdrugcrm.repository.UserRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyerCatalogServiceImpl implements BuyerCatalogService {

    private final UserRepository userRepository;
    private final PricelistRepository pricelistRepository;
    private final SpecialOfferRepository specialOfferRepository;

    public BuyerCatalogServiceImpl(UserRepository userRepository, PricelistRepository pricelistRepository, SpecialOfferRepository specialOfferRepository) {
        this.userRepository = userRepository;
        this.pricelistRepository = pricelistRepository;
        this.specialOfferRepository = specialOfferRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerCatalogDTO getCatalogForBuyer(String username) {
        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (buyer.getRole() != UserRole.ROLE_BUYER) {
            throw new IllegalArgumentException("Only buyers can access the medicine catalog");
        }
        if (buyer.getBuyerRegion() == null || buyer.getCustomerSegment() == null || buyer.getCustomerSegment().isBlank()) {
            return BuyerCatalogDTO.empty();
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<Pricelist> matches = pricelistRepository.findActiveBuyerPricelists(
                buyer.getBuyerRegion().getId(),
                buyer.getCustomerSegment().trim(),
                now
        );

        if (matches.isEmpty()) {
            return BuyerCatalogDTO.empty();
        }

        Pricelist pricelist = matches.get(0);
        Map<Long, SpecialOffer> activeOffersByVariantId = specialOfferRepository
                .findActiveOffersForPricelist(pricelist.getId(), now)
                .stream()
                .collect(Collectors.toMap(SpecialOffer::getVariantId, offer -> offer, (first, ignored) -> first));

        return BuyerCatalogDTO.fromEntity(pricelist, activeOffersByVariantId);
    }
}
