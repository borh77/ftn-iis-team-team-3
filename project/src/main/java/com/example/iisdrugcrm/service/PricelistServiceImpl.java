package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.exception.PricelistConflictException;
import com.example.iisdrugcrm.exception.PricelistLockedException;
import com.example.iisdrugcrm.exception.VariantNotFoundException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.RegionRepository;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricelistServiceImpl implements PricelistService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PricelistServiceImpl.class);
    private static final List<PricelistStatus> BLOCKING_STATUSES = List.of(PricelistStatus.IN_REVIEW, PricelistStatus.ACTIVE);

    private final PricelistRepository pricelistRepository;
    private final RegionRepository regionRepository;
    private final CatalogService catalogService;

    public PricelistServiceImpl(PricelistRepository pricelistRepository, RegionRepository regionRepository, CatalogService catalogService) {
        this.pricelistRepository = pricelistRepository;
        this.regionRepository = regionRepository;
        this.catalogService = catalogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO createCenovnik(CreatePricelistDTO dto, Long currentUserId) {
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        OffsetDateTime periodStart = dto.getPeriodStart().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime periodEnd = dto.getPeriodEnd().withOffsetSameInstant(ZoneOffset.UTC);
        if (!periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException("Period od mora biti strogo manji od perioda do.");
        }

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
        pricelist.setPeriodStart(periodStart);
        pricelist.setPeriodEnd(periodEnd);

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

        pricelist.setCreatedBy(currentUserId);
        pricelist.validateThresholds();

        lockExistingPricelists(region.getId(), pricelist.getCustomerSegment());
        validateNoBlockingOverlap(region, pricelist.getCustomerSegment(), periodStart, periodEnd);

        Pricelist saved = pricelistRepository.save(pricelist);
        LOGGER.info("Created pricelist {} for region {} and customer segment {}", saved.getId(), region.getId(), saved.getCustomerSegment());
        return PricelistResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistResponseDTO> listCenovnici() {
        return pricelistRepository.findAllByOrderByIdDesc().stream()
                .map(PricelistResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistResponseDTO> listCenovniciForUser(Long currentUserId) {
        return pricelistRepository.findAllByCreatedByOrderByIdDesc(currentUserId).stream()
                .map(PricelistResponseDTO::fromEntity)
                .toList();
    }

    private void lockExistingPricelists(Long regionId, String customerSegment) {
        try {
            pricelistRepository.lockByRegionAndCustomerSegment(regionId, customerSegment);
        } catch (PessimisticLockingFailureException
                 | LockTimeoutException
                 | PessimisticLockException exception) {
            throw new PricelistLockedException("Cenovnici za izabrani region i segment se trenutno menjaju. Pokusajte ponovo.");
        }
    }

    private void validateNoBlockingOverlap(Region region, String customerSegment, OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        List<Pricelist> conflicts = pricelistRepository.findOverlappingBlockingPricelists(
                region.getId(),
                customerSegment,
                periodStart,
                periodEnd,
                BLOCKING_STATUSES
        );
        if (conflicts.isEmpty()) {
            return;
        }

        Pricelist conflict = conflicts.get(0);
        throw new PricelistConflictException(
                "Cenovnik za region [" + region.getName() + "] i segment [" + customerSegment
                        + "] vec postoji u periodu [" + conflict.getPeriodStart().toLocalDate()
                        + " - " + conflict.getPeriodEnd().toLocalDate() + "]."
        );
    }
}
