package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistCreationStep;
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
import com.example.iisdrugcrm.service.event.PricelistActionEvent;
import java.math.BigDecimal;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public PricelistServiceImpl(PricelistRepository pricelistRepository, RegionRepository regionRepository, CatalogService catalogService, PricelistAccessService accessService, ApplicationEventPublisher eventPublisher) {
        this.pricelistRepository = pricelistRepository;
        this.regionRepository = regionRepository;
        this.catalogService = catalogService;
        this.accessService = accessService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO createCenovnik(CreatePricelistDTO dto, Long currentUserId) {
        Region region = resolveRegion(dto);
        PricelistDateRules.validateStartDateNotPast(dto.getPeriodStart());
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
        pricelist.setCreationStep(PricelistCreationStep.COMPLETED);
        pricelist.setCreationCompleted(true);
        pricelist.setLastEditedAt(OffsetDateTime.now(ZoneOffset.UTC));

        replaceItems(pricelist, dto);

        pricelist.setCreatedBy(currentUserId);
        pricelist.validateThresholds();

        lockExistingPricelists(region.getId(), pricelist.getCustomerSegment());
        validateNoBlockingOverlap(region, pricelist.getCustomerSegment(), periodStart, periodEnd);

        Pricelist saved = pricelistRepository.save(pricelist);
        publishPricelistAction(saved, currentUserId, PricelistActionType.CREATE, "Created pricelist in DRAFT status");
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
        PricelistDateRules.validateStartDateNotPast(dto.getPeriodStart());
        OffsetDateTime periodStart = utcPeriodStart(dto);
        OffsetDateTime periodEnd = utcPeriodEnd(dto);
        validatePeriod(periodStart, periodEnd);
        validateRequestedVariantsActive(dto);
        PricelistActionType updateActionType = determineUpdateAction(pricelist, dto, region, periodStart, periodEnd);

        pricelist.setRegion(region);
        pricelist.setCustomerSegment(dto.getCustomerSegment().trim());
        pricelist.setCurrency(dto.getCurrency().trim().toUpperCase());
        pricelist.setPeriodStart(periodStart);
        pricelist.setPeriodEnd(periodEnd);
        pricelist.setCreationStep(PricelistCreationStep.COMPLETED);
        pricelist.setCreationCompleted(true);
        pricelist.setLastEditedAt(OffsetDateTime.now(ZoneOffset.UTC));
        replaceItems(pricelist, dto);
        pricelist.validateThresholds();

        lockExistingPricelists(region.getId(), pricelist.getCustomerSegment());
        validateNoBlockingOverlapExcludingCurrent(pricelist);

        Pricelist saved = pricelistRepository.save(pricelist);
        publishPricelistAction(saved, currentUserId, updateActionType, updateDescription(updateActionType));
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
        newVersion.setCreationStep(PricelistCreationStep.COMPLETED);
        newVersion.setCreationCompleted(true);
        newVersion.setLastEditedAt(OffsetDateTime.now(ZoneOffset.UTC));
        int currentVersion = maxVersion != null && maxVersion > 0
                ? maxVersion
                : source.getVersionNumber() != null ? source.getVersionNumber() : 1;
        newVersion.setVersionNumber(currentVersion + 1);
        PricelistDateRules.validateStartDateNotPast(newVersion.getPeriodStart());

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
        publishPricelistAction(saved, currentUserId, PricelistActionType.CREATE_VERSION, "Created new pricelist version");
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
        return changeStatus(pricelist, dto, pricelist.getCreatedBy());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO changeStatus(Long id, ChangePricelistStatusDTO dto, Long currentUserId) {
        return changeStatus(id, dto, currentUserId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PricelistResponseDTO changeStatus(Long id, ChangePricelistStatusDTO dto, Long currentUserId, boolean currentUserAdmin) {
        Pricelist pricelist = pricelistRepository.findById(id)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        validateStatusChangeAccess(pricelist, dto.getTargetStatus(), currentUserId, currentUserAdmin);
        return changeStatus(pricelist, dto, currentUserId);
    }

    private PricelistResponseDTO changeStatus(Pricelist pricelist, ChangePricelistStatusDTO dto, Long currentUserId) {
        if (pricelist.getStatus() == PricelistStatus.DRAFT
                && dto.getTargetStatus() == PricelistStatus.IN_REVIEW
                && !pricelist.isCreationCompleted()) {
            throw new IllegalArgumentException("Pricelist was not completed through the wizard and cannot be submitted for review.");
        }
        if ((pricelist.getStatus() == PricelistStatus.DRAFT && dto.getTargetStatus() == PricelistStatus.IN_REVIEW)
                || (pricelist.getStatus() == PricelistStatus.IN_REVIEW && dto.getTargetStatus() == PricelistStatus.ACTIVE)) {
            PricelistDateRules.validateStartDateNotPast(pricelist.getPeriodStart());
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
        publishPricelistAction(
                saved,
                currentUserId,
                PricelistActionType.STATUS_CHANGE,
                "Changed status from " + previousStatus + " to " + saved.getStatus(),
                previousStatus,
                saved.getStatus()
        );
        LOGGER.info("Changed pricelist {} status from {} to {}", saved.getId(), previousStatus, saved.getStatus());
        return toResponse(saved, currentUserId, accessService.canCollaborate(saved, currentUserId));
    }

    private void validateStatusChangeAccess(Pricelist pricelist, PricelistStatus targetStatus, Long currentUserId, boolean currentUserAdmin) {
        if (pricelist.getStatus() == PricelistStatus.IN_REVIEW && targetStatus == PricelistStatus.ACTIVE) {
            accessService.validateActivationReviewer(pricelist, currentUserId, currentUserAdmin);
            return;
        }
        accessService.validateOwnerOnly(pricelist, currentUserId);
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
        Pricelist saved = pricelistRepository.save(pricelist);
        publishPricelistAction(saved, currentUserId, PricelistActionType.REPLACE_ITEM, "Replaced pricelist item");
        return toResponse(saved, currentUserId, accessService.canCollaborate(saved, currentUserId));
    }

    private void lockExistingPricelists(Long regionId, String customerSegment) {
        try {
            pricelistRepository.lockByRegionAndCustomerSegment(regionId, customerSegment);
        } catch (PessimisticLockingFailureException
                 | LockTimeoutException
                 | PessimisticLockException exception) {
            throw new PricelistLockedException("Pricelists for the selected region and segment are currently being changed. Please try again.");
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
            throw new IllegalArgumentException("Period start must be strictly before period end.");
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
            throw new VariantNotFoundException("Variants " + missingVariantIds + " do not exist or are not active in the catalog");
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

    private PricelistActionType determineUpdateAction(Pricelist pricelist, CreatePricelistDTO dto, Region region, OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        if (itemsChanged(pricelist, dto)) {
            return PricelistActionType.UPDATE_ITEMS;
        }
        if (thresholdsChanged(pricelist, dto)) {
            return PricelistActionType.UPDATE_THRESHOLDS;
        }
        if (metadataChanged(pricelist, dto, region, periodStart, periodEnd)) {
            return PricelistActionType.UPDATE_METADATA;
        }
        return PricelistActionType.UPDATE_METADATA;
    }

    private boolean metadataChanged(Pricelist pricelist, CreatePricelistDTO dto, Region region, OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        return !Objects.equals(pricelist.getRegion().getId(), region.getId())
                || !Objects.equals(pricelist.getCustomerSegment(), dto.getCustomerSegment().trim())
                || !Objects.equals(pricelist.getCurrency(), dto.getCurrency().trim().toUpperCase())
                || !Objects.equals(pricelist.getPeriodStart(), periodStart)
                || !Objects.equals(pricelist.getPeriodEnd(), periodEnd);
    }

    private boolean itemsChanged(Pricelist pricelist, CreatePricelistDTO dto) {
        if (pricelist.getItems().size() != dto.getItems().size()) {
            return true;
        }
        for (int index = 0; index < pricelist.getItems().size(); index++) {
            PricelistItem item = pricelist.getItems().get(index);
            CreatePricelistDTO.PricelistItemDTO itemDTO = dto.getItems().get(index);
            if (!Objects.equals(item.getVariantId(), itemDTO.getVariantId())
                    || !Objects.equals(item.getVariantName(), itemDTO.getVariantName().trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean thresholdsChanged(Pricelist pricelist, CreatePricelistDTO dto) {
        for (int itemIndex = 0; itemIndex < pricelist.getItems().size(); itemIndex++) {
            List<QuantityThreshold> thresholds = pricelist.getItems().get(itemIndex).getThresholds();
            List<CreatePricelistDTO.QuantityThresholdDTO> thresholdDTOs = dto.getItems().get(itemIndex).getThresholds();
            if (thresholds.size() != thresholdDTOs.size()) {
                return true;
            }
            for (int thresholdIndex = 0; thresholdIndex < thresholds.size(); thresholdIndex++) {
                QuantityThreshold threshold = thresholds.get(thresholdIndex);
                CreatePricelistDTO.QuantityThresholdDTO thresholdDTO = thresholdDTOs.get(thresholdIndex);
                if (!Objects.equals(threshold.getQuantityFrom(), thresholdDTO.getQuantityFrom())
                        || !Objects.equals(threshold.getQuantityTo(), thresholdDTO.getQuantityTo())
                        || comparePrice(threshold.getPrice(), thresholdDTO.getPrice()) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private int comparePrice(BigDecimal first, BigDecimal second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null || second == null) {
            return 1;
        }
        return first.compareTo(second);
    }

    private String updateDescription(PricelistActionType actionType) {
        return switch (actionType) {
            case UPDATE_ITEMS -> "Updated pricelist items";
            case UPDATE_THRESHOLDS -> "Updated pricelist price thresholds";
            default -> "Updated pricelist metadata";
        };
    }

    private void publishPricelistAction(Pricelist pricelist, Long userId, PricelistActionType actionType, String description) {
        publishPricelistAction(pricelist, userId, actionType, description, null, null);
    }

    private void publishPricelistAction(
            Pricelist pricelist,
            Long userId,
            PricelistActionType actionType,
            String description,
            PricelistStatus statusFrom,
            PricelistStatus statusTo
    ) {
        if (pricelist.getId() == null || userId == null) {
            LOGGER.debug("Skipping pricelist activity event for pricelist {} and user {}", pricelist.getId(), userId);
            return;
        }
        eventPublisher.publishEvent(new PricelistActionEvent(
                pricelist.getId(),
                userId,
                resolveTeamId(pricelist),
                actionType,
                description,
                statusFrom,
                statusTo
        ));
    }

    private Long resolveTeamId(Pricelist pricelist) {
        return pricelist.getTeam() != null ? pricelist.getTeam().getId() : null;
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
                "Pricelist for region [" + region.getName() + "] and segment [" + customerSegment
                        + "] already exists in period [" + conflict.getPeriodStart().toLocalDate()
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
        PricelistResponseDTO response = PricelistResponseDTO.fromEntity(pricelist, currentUserId, canCollaborate, activeVariantsFor(pricelist));
        response.setCanActivate(pricelist.getStatus() == PricelistStatus.IN_REVIEW
                && accessService.canActivateAsReviewer(pricelist, currentUserId));
        return response;
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
                "Pricelist for region [" + pricelist.getRegion().getName() + "] and segment [" + pricelist.getCustomerSegment()
                        + "] already exists in period [" + conflict.getPeriodStart().toLocalDate()
                        + " - " + conflict.getPeriodEnd().toLocalDate() + "]."
        );
    }
}
