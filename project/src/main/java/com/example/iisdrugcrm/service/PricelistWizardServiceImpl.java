package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.PricelistTeam;
import com.example.iisdrugcrm.domain.Region;
import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.domain.pricelist.PricelistCreationStep;
import com.example.iisdrugcrm.domain.pricelist.PricelistItem;
import com.example.iisdrugcrm.domain.pricelist.QuantityThreshold;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistWizardStateDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistWizardSummaryDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveBasicInfoStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveItemsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveTeamAccessStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveThresholdsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.StartPricelistWizardResponseDTO;
import com.example.iisdrugcrm.exception.PricelistConflictException;
import com.example.iisdrugcrm.exception.PricelistNotFoundException;
import com.example.iisdrugcrm.exception.VariantNotFoundException;
import com.example.iisdrugcrm.repository.PricelistRepository;
import com.example.iisdrugcrm.repository.PricelistTeamRepository;
import com.example.iisdrugcrm.repository.RegionRepository;
import com.example.iisdrugcrm.service.event.PricelistActionEvent;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricelistWizardServiceImpl implements PricelistWizardService {

    private static final List<PricelistStatus> BLOCKING_STATUSES = List.of(PricelistStatus.IN_REVIEW, PricelistStatus.ACTIVE);

    private final PricelistRepository pricelistRepository;
    private final RegionRepository regionRepository;
    private final PricelistTeamRepository teamRepository;
    private final CatalogService catalogService;
    private final PricelistAccessService accessService;
    private final ApplicationEventPublisher eventPublisher;

    public PricelistWizardServiceImpl(
            PricelistRepository pricelistRepository,
            RegionRepository regionRepository,
            PricelistTeamRepository teamRepository,
            CatalogService catalogService,
            PricelistAccessService accessService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.pricelistRepository = pricelistRepository;
        this.regionRepository = regionRepository;
        this.teamRepository = teamRepository;
        this.catalogService = catalogService;
        this.accessService = accessService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public StartPricelistWizardResponseDTO startWizard(Long currentUserId) {
        Region placeholderRegion = regionRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("At least one region is required before starting the pricelist wizard."));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Pricelist pricelist = new Pricelist();
        pricelist.setRegion(placeholderRegion);
        pricelist.setCustomerSegment("UNDEFINED");
        pricelist.setCurrency("RSD");
        pricelist.setStatus(PricelistStatus.DRAFT);
        pricelist.setPeriodStart(now);
        pricelist.setPeriodEnd(now.plusDays(1));
        pricelist.setCreatedBy(currentUserId);
        pricelist.setVersionNumber(1);
        pricelist.setCreationStep(PricelistCreationStep.BASIC_INFO);
        pricelist.setCreationCompleted(false);
        pricelist.setLastEditedAt(now);

        Pricelist saved = pricelistRepository.save(pricelist);
        publishAction(saved, currentUserId, PricelistActionType.CREATE, "Started pricelist creation wizard");
        return new StartPricelistWizardResponseDTO(saved.getId(), toState(saved, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricelistWizardStateDTO> getDrafts(Long currentUserId) {
        return pricelistRepository.findAllByCreatedByAndCreationCompletedFalseOrderByLastEditedAtDescIdDesc(currentUserId).stream()
                .filter(pricelist -> pricelist.getStatus() == PricelistStatus.DRAFT)
                .map(pricelist -> toState(pricelist, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PricelistWizardStateDTO getWizardState(Long pricelistId, Long currentUserId) {
        return toState(requireEditableDraft(pricelistId, currentUserId), currentUserId);
    }

    @Override
    @Transactional
    public PricelistWizardStateDTO saveBasicInfo(Long pricelistId, SaveBasicInfoStepDTO dto, Long currentUserId) {
        Pricelist pricelist = requireEditableDraft(pricelistId, currentUserId);
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));
        OffsetDateTime periodStart = dto.getPeriodStart().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime periodEnd = dto.getPeriodEnd().withOffsetSameInstant(ZoneOffset.UTC);
        validatePeriod(periodStart, periodEnd);

        pricelist.setRegion(region);
        pricelist.setCustomerSegment(dto.getCustomerSegment().trim());
        pricelist.setCurrency(dto.getCurrency().trim().toUpperCase());
        pricelist.setPeriodStart(periodStart);
        pricelist.setPeriodEnd(periodEnd);
        markProgress(pricelist, PricelistCreationStep.TEAM_ACCESS);

        Pricelist saved = pricelistRepository.save(pricelist);
        publishAction(saved, currentUserId, PricelistActionType.UPDATE_METADATA, "Updated pricelist wizard basic information");
        return toState(saved, currentUserId);
    }

    @Override
    @Transactional
    public PricelistWizardStateDTO saveTeamAccess(Long pricelistId, SaveTeamAccessStepDTO dto, Long currentUserId) {
        Pricelist pricelist = requireEditableDraft(pricelistId, currentUserId);
        PricelistTeam team = null;
        if (dto.getTeamId() != null) {
            team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team not found"));
            if (!team.getLeaderId().equals(currentUserId) && !team.getMemberIds().contains(currentUserId)) {
                throw new AccessDeniedException("You cannot assign this pricelist to the selected team.");
            }
        }

        pricelist.setTeam(team);
        markProgress(pricelist, PricelistCreationStep.ITEMS);
        return toState(pricelistRepository.save(pricelist), currentUserId);
    }

    @Override
    @Transactional
    public PricelistWizardStateDTO saveItems(Long pricelistId, SaveItemsStepDTO dto, Long currentUserId) {
        Pricelist pricelist = requireEditableDraft(pricelistId, currentUserId);
        validateUniqueVariants(dto.getItems().stream().map(SaveItemsStepDTO.PricelistWizardItemDTO::getVariantId).toList());
        Map<Long, CatalogVariantDTO> activeVariants = resolveActiveVariants(dto.getItems().stream()
                .map(SaveItemsStepDTO.PricelistWizardItemDTO::getVariantId)
                .toList());

        pricelist.getItems().clear();
        for (SaveItemsStepDTO.PricelistWizardItemDTO itemDTO : dto.getItems()) {
            CatalogVariantDTO variant = activeVariants.get(itemDTO.getVariantId());
            PricelistItem item = new PricelistItem();
            item.setVariantId(itemDTO.getVariantId());
            item.setVariantName(normalizeVariantName(itemDTO.getVariantName(), variant));
            pricelist.addItem(item);
        }
        markProgress(pricelist, PricelistCreationStep.THRESHOLDS);

        Pricelist saved = pricelistRepository.save(pricelist);
        publishAction(saved, currentUserId, PricelistActionType.UPDATE_ITEMS, "Updated pricelist wizard items");
        return toState(saved, currentUserId);
    }

    @Override
    @Transactional
    public PricelistWizardStateDTO saveThresholds(Long pricelistId, SaveThresholdsStepDTO dto, Long currentUserId) {
        Pricelist pricelist = requireEditableDraft(pricelistId, currentUserId);
        Map<Long, PricelistItem> itemsByVariantId = itemsByVariantId(pricelist);
        if (itemsByVariantId.size() != dto.getItems().size()) {
            throw new IllegalArgumentException("Thresholds must be provided for every selected item.");
        }

        Set<Long> seenVariantIds = new HashSet<>();
        for (SaveThresholdsStepDTO.PricelistItemThresholdsDTO itemDTO : dto.getItems()) {
            if (!seenVariantIds.add(itemDTO.getVariantId())) {
                throw new IllegalArgumentException("Duplicate variant thresholds are not allowed.");
            }
            PricelistItem item = itemsByVariantId.get(itemDTO.getVariantId());
            if (item == null) {
                throw new IllegalArgumentException("Thresholds reference an item that is not part of this draft.");
            }
            item.setThresholds(toThresholds(itemDTO.getThresholds()));
            item.validateThresholds();
        }

        markProgress(pricelist, PricelistCreationStep.REVIEW);
        Pricelist saved = pricelistRepository.save(pricelist);
        publishAction(saved, currentUserId, PricelistActionType.UPDATE_THRESHOLDS, "Updated pricelist wizard price thresholds");
        return toState(saved, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PricelistWizardSummaryDTO getSummary(Long pricelistId, Long currentUserId) {
        Pricelist pricelist = requireEditableDraft(pricelistId, currentUserId);
        return PricelistWizardSummaryDTO.of(pricelist, toResponse(pricelist, currentUserId), validationMessages(pricelist));
    }

    @Override
    @Transactional
    public PricelistWizardStateDTO finishWizard(Long pricelistId, Long currentUserId) {
        Pricelist pricelist = requireEditableDraft(pricelistId, currentUserId);
        List<String> validationMessages = validationMessages(pricelist);
        if (!validationMessages.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", validationMessages));
        }

        validateAllVariantsActive(pricelist);
        pricelist.validateThresholds();
        validateNoBlockingOverlapExcludingCurrent(pricelist);
        pricelist.setCreationCompleted(true);
        pricelist.setCreationStep(PricelistCreationStep.COMPLETED);
        pricelist.setLastEditedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Pricelist saved = pricelistRepository.save(pricelist);
        publishAction(saved, currentUserId, PricelistActionType.UPDATE_METADATA, "Completed pricelist creation wizard");
        return toState(saved, currentUserId);
    }

    private Pricelist requireEditableDraft(Long pricelistId, Long currentUserId) {
        Pricelist pricelist = pricelistRepository.findById(pricelistId)
                .orElseThrow(() -> new PricelistNotFoundException("Pricelist not found"));
        if (!accessService.canCollaborate(pricelist, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this pricelist.");
        }
        if (pricelist.getStatus() != PricelistStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft pricelists can be edited through the wizard.");
        }
        return pricelist;
    }

    private void markProgress(Pricelist pricelist, PricelistCreationStep nextStep) {
        pricelist.setCreationCompleted(false);
        pricelist.setCreationStep(nextStep);
        pricelist.setLastEditedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    private void validatePeriod(OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        if (!periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException("Period start must be strictly before period end.");
        }
    }

    private void validateUniqueVariants(Collection<Long> variantIds) {
        Set<Long> unique = new HashSet<>();
        for (Long variantId : variantIds) {
            if (!unique.add(variantId)) {
                throw new IllegalArgumentException("Duplicate variants are not allowed in the same pricelist.");
            }
        }
    }

    private Map<Long, CatalogVariantDTO> resolveActiveVariants(Collection<Long> variantIds) {
        Map<Long, CatalogVariantDTO> activeVariants = catalogService.findActiveVariantsByIds(variantIds);
        List<Long> missingVariantIds = variantIds.stream()
                .filter(variantId -> !activeVariants.containsKey(variantId))
                .toList();
        if (!missingVariantIds.isEmpty()) {
            throw new VariantNotFoundException("Variants " + missingVariantIds + " do not exist or are not active in the catalog");
        }
        return activeVariants;
    }

    private String normalizeVariantName(String requestedName, CatalogVariantDTO variant) {
        String trimmed = requestedName == null ? "" : requestedName.trim();
        return trimmed.isBlank() ? variant.getName() : trimmed;
    }

    private Map<Long, PricelistItem> itemsByVariantId(Pricelist pricelist) {
        return pricelist.getItems().stream()
                .collect(java.util.stream.Collectors.toMap(PricelistItem::getVariantId, item -> item));
    }

    private List<QuantityThreshold> toThresholds(List<SaveThresholdsStepDTO.QuantityThresholdDTO> thresholdDTOs) {
        List<QuantityThreshold> thresholds = new ArrayList<>();
        for (SaveThresholdsStepDTO.QuantityThresholdDTO thresholdDTO : thresholdDTOs) {
            QuantityThreshold threshold = new QuantityThreshold();
            threshold.setQuantityFrom(thresholdDTO.getQuantityFrom());
            threshold.setQuantityTo(thresholdDTO.getQuantityTo());
            threshold.setPrice(thresholdDTO.getPrice());
            thresholds.add(threshold);
        }
        return thresholds;
    }

    private List<String> validationMessages(Pricelist pricelist) {
        List<String> messages = new ArrayList<>();
        if (pricelist.getRegion() == null) {
            messages.add("Region is required.");
        }
        if (pricelist.getCustomerSegment() == null || pricelist.getCustomerSegment().isBlank() || "UNDEFINED".equals(pricelist.getCustomerSegment())) {
            messages.add("Customer segment is required.");
        }
        if (pricelist.getCurrency() == null || pricelist.getCurrency().isBlank()) {
            messages.add("Currency is required.");
        }
        if (pricelist.getPeriodStart() == null || pricelist.getPeriodEnd() == null || !pricelist.getPeriodStart().isBefore(pricelist.getPeriodEnd())) {
            messages.add("Valid period start and end are required.");
        }
        if (pricelist.getItems().isEmpty()) {
            messages.add("At least one item is required.");
        }
        for (PricelistItem item : pricelist.getItems()) {
            if (item.getThresholds() == null || item.getThresholds().isEmpty()) {
                messages.add("Each item must have thresholds.");
                break;
            }
        }
        try {
            pricelist.validateThresholds();
        } catch (RuntimeException exception) {
            messages.add(exception.getMessage());
        }
        return messages;
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

    private PricelistWizardStateDTO toState(Pricelist pricelist, Long currentUserId) {
        return PricelistWizardStateDTO.fromEntity(pricelist, toResponse(pricelist, currentUserId));
    }

    private PricelistResponseDTO toResponse(Pricelist pricelist, Long currentUserId) {
        return PricelistResponseDTO.fromEntity(pricelist, currentUserId, accessService.canCollaborate(pricelist, currentUserId), activeVariantsFor(pricelist));
    }

    private Map<Long, CatalogVariantDTO> activeVariantsFor(Pricelist pricelist) {
        List<Long> variantIds = pricelist.getItems().stream()
                .map(PricelistItem::getVariantId)
                .distinct()
                .toList();
        return catalogService.findActiveVariantsByIds(variantIds);
    }

    private void publishAction(Pricelist pricelist, Long userId, PricelistActionType actionType, String description) {
        if (pricelist.getId() == null || userId == null) {
            return;
        }
        eventPublisher.publishEvent(new PricelistActionEvent(
                pricelist.getId(),
                userId,
                pricelist.getTeam() != null ? pricelist.getTeam().getId() : null,
                actionType,
                description
        ));
    }
}
