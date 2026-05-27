package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.exception.VariantNotFoundException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.RegionRepository;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricelistServiceImpl implements PricelistService {

    private final PricelistRepository pricelistRepository;
    private final RegionRepository regionRepository;
    private final CatalogService catalogService;

    public PricelistServiceImpl(PricelistRepository pricelistRepository, RegionRepository regionRepository, CatalogService catalogService) {
        this.pricelistRepository = pricelistRepository;
        this.regionRepository = regionRepository;
        this.catalogService = catalogService;
    }

    @Override
    @Transactional
    public PricelistResponseDTO createCenovnik(CreatePricelistDTO dto) {
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        List<Long> requestedVariantIds = dto.getItems().stream()
                .map(CreatePricelistDTO.PricelistItemDTO::getVariantId)
                .distinct()
                .toList();

        Map<Long, ?> resolvedVariants = catalogService.findActiveVariantsByIds(requestedVariantIds);
        List<Long> missingVariantIds = requestedVariantIds.stream()
                .filter(variantId -> !resolvedVariants.containsKey(variantId))
                .toList();

        if (!missingVariantIds.isEmpty()) {
            throw new VariantNotFoundException("Varijante " + missingVariantIds + " ne postoje ili nisu aktivne u katalogu");
        }

        Pricelist pricelist = new Pricelist();
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(dto.getCustomerSegment().trim());
        pricelist.setCurrency(dto.getCurrency().trim().toUpperCase());
        pricelist.setStatus(PricelistStatus.DRAFT);
        pricelist.setPeriodStart(dto.getPeriodStart().withOffsetSameInstant(ZoneOffset.UTC));
        pricelist.setPeriodEnd(dto.getPeriodEnd().withOffsetSameInstant(ZoneOffset.UTC));

        for (CreatePricelistDTO.PricelistItemDTO itemDTO : dto.getItems()) {
            PricelistItem item = new PricelistItem();
            item.setVariantId(itemDTO.getVariantId());
            item.setVariantName(itemDTO.getVariantName().trim());

            List<QuantityThreshold> thresholds = new ArrayList<>();
            for (CreatePricelistDTO.QuantityThresholdDTO thresholdDTO : itemDTO.getThresholds()) {
                QuantityThreshold threshold = new QuantityThreshold();
                threshold.setQuantityFrom(thresholdDTO.getQuantityFrom());
                threshold.setQuantityTo(thresholdDTO.getQuantityTo());
                threshold.setPrice(thresholdDTO.getPrice());
                thresholds.add(threshold);
            }
            item.setThresholds(thresholds);
            pricelist.addItem(item);
        }

        return PricelistResponseDTO.fromEntity(pricelistRepository.save(pricelist));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistResponseDTO> listCenovnici() {
        return pricelistRepository.findAllByOrderByIdDesc().stream()
                .map(PricelistResponseDTO::fromEntity)
                .toList();
    }
}