package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.pricelist.ChangePricelistStatusDTO;
import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.exception.PricelistConflictException;
import com.example.iisdrugcrm.exception.PricelistLockedException;
import com.example.iisdrugcrm.exception.PricelistNotFoundException;
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
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricelistServiceImpl implements PricelistService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PricelistServiceImpl.class);
    private static final List<PricelistStatus> BLOCKING_STATUSES = List.of(PricelistStatus.IN_REVIEW, PricelistStatus.ACTIVE);

    private final PricelistRepository pricelistRepository;
    private final RegionRepository regionRepository;
    private final CatalogService catalogService;
    private final PricelistAccessService accessService;

    public PricelistServiceImpl(PricelistRepository pricelistRepository, RegionRepository regionRepository, CatalogService catalogService, PricelistAccessService accessService) {
        this.pricelistRepository = pricelistRepository;
        this.regionRepository = regionRepository;
        this.catalogService = catalogService;
        this.accessService = accessService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO createCenovnik(CreatePricelistDTO dto, Long currentUserId) {
        Region region = resolveRegion(dto);
        OffsetDateTime periodStart = utcPeriodStart(dto);
        OffsetDateTime periodEnd = utcPeriodEnd(dto);
        validatePeriod(periodStart, periodEnd);
        validateRequestedVariantsActive(dto);

        Pricelist pricelist = new Pricelist();
        pricelist.setRegion(region);
        pricelist.setCustomerSegment(dto.getCustomerSegment().trim());
        pricelist.setCurrency(dto.getCurrency().trim().toUpperCase());
        pricelist.setStatus(PricelistStatus.DRAFT);
        pricelist.setPeriodStart(periodStart);
        pricelist.setPeriodEnd(periodEnd);
        pricelist.setVersionNumber(1);

        replaceItems(pricelist, dto);

        pricelist.setCreatedBy(currentUserId);
        pricelist.validateThresholds();

        lockExistingPricelists(region.getId(), pricelist.getCustomerSegment());
        validateNoBlockingOverlap(region, pricelist.getCustomerSegment(), periodStart, periodEnd);

        Pricelist saved = pricelistRepository.save(pricelist);
        LOGGER.info("Created pricelist {} for region {} and customer segment {}", saved.getId(), region.getId(), saved.getCustomerSegment());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PricelistResponseDTO getById(Long id, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(id)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        boolean canCollaborate = accessService.canCollaborate(pricelist, currentUserId);
        if (!canCollaborate) {
            throw new AccessDeniedException("You do not have access to this pricelist.");
        }
        return toResponse(pricelist, currentUserId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO update(Long id, CreatePricelistDTO dto, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(id)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        if (!accessService.canCollaborate(pricelist, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this pricelist.");
        }
        if (pricelist.getStatus() != PricelistStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft pricelists can be edited.");
        }

        Region region = resolveRegion(dto);
        OffsetDateTime periodStart = utcPeriodStart(dto);
        OffsetDateTime periodEnd = utcPeriodEnd(dto);
        validatePeriod(periodStart, periodEnd);
        validateRequestedVariantsActive(dto);

        pricelist.setRegion(region);
        pricelist.setCustomerSegment(dto.getCustomerSegment().trim());
        pricelist.setCurrency(dto.getCurrency().trim().toUpperCase());
        pricelist.setPeriodStart(periodStart);
        pricelist.setPeriodEnd(periodEnd);
        replaceItems(pricelist, dto);
        pricelist.validateThresholds();

        lockExistingPricelists(region.getId(), pricelist.getCustomerSegment());
        validateNoBlockingOverlapExcludingCurrent(pricelist);

        Pricelist saved = pricelistRepository.save(pricelist);
        LOGGER.info("Updated draft pricelist {}", saved.getId());
        return toResponse(saved, currentUserId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO createNewVersion(Long sourcePricelistId, Long currentUserId) {
        Pricelist source = pricelistRepository.findById(sourcePricelistId)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        accessService.validateOwnerOrTeamMember(source, currentUserId);

        if (source.getStatus() == PricelistStatus.ARCHIVED) {
            throw new IllegalArgumentException("Archived pricelists cannot be versioned.");
        }
        if (source.getStatus() == PricelistStatus.DRAFT) {
            throw new IllegalArgumentException("Draft pricelists can be edited directly.");
        }

        Long rootPricelistId = source.getRootPricelistId() != null ? source.getRootPricelistId() : source.getId();
        Integer maxVersion = pricelistRepository.findMaxVersionNumberForRoot(rootPricelistId);

        Pricelist newVersion = new Pricelist();
        newVersion.setRegion(source.getRegion());
        newVersion.setCustomerSegment(source.getCustomerSegment());
        newVersion.setCurrency(source.getCurrency());
        newVersion.setPeriodStart(source.getPeriodStart());
        newVersion.setPeriodEnd(source.getPeriodEnd());
        newVersion.setStatus(PricelistStatus.DRAFT);
        newVersion.setCreatedBy(currentUserId);
        newVersion.setParentPricelistId(source.getId());
        newVersion.setRootPricelistId(rootPricelistId);
        int currentVersion = maxVersion != null && maxVersion > 0
                ? maxVersion
                : source.getVersionNumber() != null ? source.getVersionNumber() : 1;
        newVersion.setVersionNumber(currentVersion + 1);

        for (PricelistItem sourceItem : source.getItems()) {
            PricelistItem item = new PricelistItem();
            item.setVariantId(sourceItem.getVariantId());
            item.setVariantName(sourceItem.getVariantName());

            List<QuantityThreshold> thresholds = new ArrayList<>();
            for (QuantityThreshold sourceThreshold : sourceItem.getThresholds()) {
                QuantityThreshold threshold = new QuantityThreshold();
                threshold.setQuantityFrom(sourceThreshold.getQuantityFrom());
                threshold.setQuantityTo(sourceThreshold.getQuantityTo());
                threshold.setPrice(sourceThreshold.getPrice());
                thresholds.add(threshold);
            }
            item.setThresholds(thresholds);
            newVersion.addItem(item);
        }

        Pricelist saved = pricelistRepository.save(newVersion);
        LOGGER.info("Created draft version {} from pricelist {}", saved.getId(), source.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistResponseDTO> listCenovnici() {
        return pricelistRepository.findAllByOrderByIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistResponseDTO> listCenovniciForUser(Long currentUserId) {
        return pricelistRepository.findAllByCreatedByOrderByIdDesc(currentUserId).stream()
                .map(pricelist -> toResponse(pricelist, currentUserId, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistResponseDTO> listTeamCenovniciForUser(Long currentUserId) {
        Set<Long> accessibleCreatorIds = accessService.accessibleCreatorIds(currentUserId);
        return pricelistRepository.findAllByCreatedByInOrderByIdDesc(accessibleCreatorIds).stream()
                .map(pricelist -> toResponse(pricelist, currentUserId, accessService.canCollaborate(pricelist, currentUserId)))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO changeStatus(Long id, ChangePricelistStatusDTO dto) {
        Pricelist pricelist = pricelistRepository.findById(id)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        return changeStatus(pricelist, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO changeStatus(Long id, ChangePricelistStatusDTO dto, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(id)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        accessService.validateOwnerOnly(pricelist, currentUserId);
        return changeStatus(pricelist, dto);
    }

    private PricelistResponseDTO changeStatus(Pricelist pricelist, ChangePricelistStatusDTO dto) {
        if ((pricelist.getStatus() == PricelistStatus.DRAFT && dto.getTargetStatus() == PricelistStatus.IN_REVIEW)
                || (pricelist.getStatus() == PricelistStatus.IN_REVIEW && dto.getTargetStatus() == PricelistStatus.ACTIVE)) {
            validateAllVariantsActive(pricelist);
        }
        if (pricelist.getStatus() == PricelistStatus.IN_REVIEW && dto.getTargetStatus() == PricelistStatus.ACTIVE) {
            lockExistingPricelists(pricelist.getRegion().getId(), pricelist.getCustomerSegment());
            validateNoBlockingOverlapExcludingCurrent(pricelist);
        }

        PricelistStatus previousStatus = pricelist.getStatus();
        pricelist.changeStatus(dto.getTargetStatus(), dto.getReason());

        if (previousStatus == PricelistStatus.IN_REVIEW && dto.getTargetStatus() == PricelistStatus.DRAFT) {
            LOGGER.info("Pricelist {} returned to DRAFT. Reason: {}", pricelist.getId(), dto.getReason().trim());
        }

        Pricelist saved = pricelistRepository.save(pricelist);
        LOGGER.info("Changed pricelist {} status from {} to {}", saved.getId(), previousStatus, saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO replaceItemVariant(Long pricelistId, Long itemId, Long replacementVariantId, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(pricelistId)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        accessService.validateOwnerOrTeamMember(pricelist, currentUserId);
        if (pricelist.getStatus() != PricelistStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft pricelists can replace withdrawn variants.");
        }

        PricelistItem item = pricelist.getItems().stream()
                .filter(candidate -> itemId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pricelist item not found."));

        boolean duplicate = pricelist.getItems().stream()
                .anyMatch(candidate -> !itemId.equals(candidate.getId()) && replacementVariantId.equals(candidate.getVariantId()));
        if (duplicate) {
            throw new IllegalArgumentException("Selected replacement variant already exists in this pricelist.");
        }

        Map<Long, CatalogVariantDTO> activeVariants = catalogService.findActiveVariantsByIds(List.of(replacementVariantId));
        CatalogVariantDTO replacement = activeVariants.get(replacementVariantId);
        if (replacement == null || !replacement.isActive()) {
            throw new IllegalArgumentException("Selected replacement variant is not active.");
        }

        item.setVariantId(replacement.getId());
        item.setVariantName(replacement.getName());
        return toResponse(pricelistRepository.save(pricelist), currentUserId, accessService.canCollaborate(pricelist, currentUserId));
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

    private Region resolveRegion(CreatePricelistDTO dto) {
        return regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));
    }

    private OffsetDateTime utcPeriodStart(CreatePricelistDTO dto) {
        return dto.getPeriodStart().withOffsetSameInstant(ZoneOffset.UTC);
    }

    private OffsetDateTime utcPeriodEnd(CreatePricelistDTO dto) {
        return dto.getPeriodEnd().withOffsetSameInstant(ZoneOffset.UTC);
    }

    private void validatePeriod(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        if (!periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException("Period od mora biti strogo manji od perioda do.");
        }
    }

    private void validateRequestedVariantsActive(CreatePricelistDTO dto) {
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
    }

    private void replaceItems(Pricelist pricelist, CreatePricelistDTO dto) {
        pricelist.getItems().clear();
        for (CreatePricelistDTO.PricelistItemDTO itemDTO : dto.getItems()) {
            PricelistItem item = new PricelistItem();
            item.setVariantId(itemDTO.getVariantId());
            item.setVariantName(itemDTO.getVariantName().trim());
            item.setThresholds(toThresholds(itemDTO.getThresholds()));
            pricelist.addItem(item);
        }
    }

    private List<QuantityThreshold> toThresholds(List<CreatePricelistDTO.QuantityThresholdDTO> thresholdDTOs) {
        List<QuantityThreshold> thresholds = new ArrayList<>();
        for (CreatePricelistDTO.QuantityThresholdDTO thresholdDTO : thresholdDTOs) {
            QuantityThreshold threshold = new QuantityThreshold();
            threshold.setQuantityFrom(thresholdDTO.getQuantityFrom());
            threshold.setQuantityTo(thresholdDTO.getQuantityTo());
            threshold.setPrice(thresholdDTO.getPrice());
            thresholds.add(threshold);
        }
        return thresholds;
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

    private void validateAllVariantsActive(Pricelist pricelist) {
        List<Long> variantIds = pricelist.getItems().stream()
                .map(PricelistItem::getVariantId)
                .distinct()
                .toList();
        Map<Long, CatalogVariantDTO> activeVariants = catalogService.findActiveVariantsByIds(variantIds);
        if (variantIds.stream().anyMatch(variantId -> !activeVariants.containsKey(variantId))) {
            throw new IllegalArgumentException("Pricelist contains inactive catalog variants. Replace them before continuing.");
        }
    }

    private PricelistResponseDTO toResponse(Pricelist pricelist) {
        return PricelistResponseDTO.fromEntity(pricelist, activeVariantsFor(pricelist));
    }

    private PricelistResponseDTO toResponse(Pricelist pricelist, Long currentUserId, boolean canCollaborate) {
        return PricelistResponseDTO.fromEntity(pricelist, currentUserId, canCollaborate, activeVariantsFor(pricelist));
    }

    private Map<Long, CatalogVariantDTO> activeVariantsFor(Pricelist pricelist) {
        List<Long> variantIds = pricelist.getItems().stream()
                .map(PricelistItem::getVariantId)
                .distinct()
                .toList();
        return catalogService.findActiveVariantsByIds(variantIds);
    }

    private void validateNoBlockingOverlapExcludingCurrent(Pricelist pricelist) {
        List<Pricelist> conflicts = pricelistRepository.findOverlappingBlockingPricelistsExcludingCurrent(
                pricelist.getRegion().getId(),
                pricelist.getCustomerSegment(),
                pricelist.getPeriodStart(),
                pricelist.getPeriodEnd(),
                BLOCKING_STATUSES,
                pricelist.getId()
        );
        if (conflicts.isEmpty()) {
            return;
        }

        Pricelist conflict = conflicts.get(0);
        throw new PricelistConflictException(
                "Cenovnik za region [" + pricelist.getRegion().getName() + "] i segment [" + pricelist.getCustomerSegment()
                        + "] vec postoji u periodu [" + conflict.getPeriodStart().toLocalDate()
                        + " - " + conflict.getPeriodEnd().toLocalDate() + "]."
        );
    }
}
