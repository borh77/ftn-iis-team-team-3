package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.Variant;
import com.example.iisdrugcrm.domain.portfolio.VariantVersion;
import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusRequestDTO;
import com.example.iisdrugcrm.exception.PortfolioDuplicateResourceException;
import com.example.iisdrugcrm.exception.PortfolioResourceNotFoundException;
import com.example.iisdrugcrm.repository.portfolio.VariantRepository;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionLifecycleHistory;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;
import com.example.iisdrugcrm.repository.portfolio.VariantVersionLifecycleHistoryRepository;

import java.util.List;

@Service
public class VariantVersionServiceImpl implements VariantVersionService {

    private final VariantVersionRepository variantVersionRepository;
    private final VariantRepository variantRepository;
    private final VariantVersionLifecycleHistoryRepository lifecycleHistoryRepository;

    public VariantVersionServiceImpl(
            VariantVersionRepository variantVersionRepository,
            VariantRepository variantRepository,
            VariantVersionLifecycleHistoryRepository lifecycleHistoryRepository
    ) {
        this.variantVersionRepository = variantVersionRepository;
        this.variantRepository = variantRepository;
        this.lifecycleHistoryRepository = lifecycleHistoryRepository;
    }

    @Override
public List<VariantVersionResponseDTO> getVariantVersions(
        String search,
        Long variantId,
        VariantVersionStatus status
) {
    List<VariantVersion> versions;

    if (search != null && !search.isBlank()) {
        versions = variantVersionRepository.searchByTextWithRelations(search.trim());
    } else if (variantId != null) {
        versions = variantVersionRepository.findByVariantIdWithRelations(variantId);
    } else if (status != null) {
        versions = variantVersionRepository.findByStatusWithRelations(status);
    } else {
        versions = variantVersionRepository.findAllWithRelations();
    }

    return versions.stream()
            .map(VariantVersionResponseDTO::fromEntity)
            .toList();
}

    @Override
    @Transactional
    public VariantVersionResponseDTO create(VariantVersionRequestDTO dto) {
        Variant variant = getVariant(dto.getVariantId());

        if (variantVersionRepository.existsByVariantIdAndVersionLabelIgnoreCase(
                dto.getVariantId(),
                dto.getVersionLabel()
        )) {
            throw new PortfolioDuplicateResourceException(
                    "Variant version label already exists for this variant"
            );
        }

        VariantVersion version = new VariantVersion(
                variant,
                dto.getVersionLabel(),
                dto.getDescription()
        );

        return VariantVersionResponseDTO.fromEntity(
                variantVersionRepository.save(version)
        );
    }

    @Override
    @Transactional
    public VariantVersionResponseDTO update(Long id, VariantVersionRequestDTO dto) {
        VariantVersion version = getVariantVersion(id);

        if (version.getStatus() != VariantVersionStatus.DEVELOPMENT) {
            throw new IllegalStateException(
                    "Only DEVELOPMENT versions can be updated"
            );
        }

        if (!version.getVariant().getId().equals(dto.getVariantId())) {
            throw new IllegalStateException(
                    "Variant cannot be changed for an existing version"
            );
        }

        version.update(dto.getDescription());

        return VariantVersionResponseDTO.fromEntity(
                variantVersionRepository.save(version)
        );
    }

    @Override
    @Transactional
    public VariantVersionResponseDTO changeStatus(
            Long id,
            VariantVersionStatusRequestDTO dto
    ) {
        VariantVersion version = getVariantVersion(id);

        VariantVersionStatus oldStatus = version.getStatus();
        VariantVersionStatus newStatus = dto.getStatus();

        if (oldStatus == newStatus) {
            return VariantVersionResponseDTO.fromEntity(version);
        }

        if (newStatus == VariantVersionStatus.ACTIVE) {
            archiveCurrentActiveVersion(version);
        }

        version.changeStatus(newStatus);

        lifecycleHistoryRepository.save(
                new VariantVersionLifecycleHistory(
                        version,
                        oldStatus,
                        newStatus,
                        1L,
                        "Variant version status changed manually",
                        false
                )
        );

        return VariantVersionResponseDTO.fromEntity(
                variantVersionRepository.save(version)
        );
    }

    private void archiveCurrentActiveVersion(VariantVersion newActiveVersion) {
        variantVersionRepository
                .findByVariantIdAndStatus(
                        newActiveVersion.getVariant().getId(),
                        VariantVersionStatus.ACTIVE
                )
                .ifPresent(currentActiveVersion -> {
                    if (!currentActiveVersion.getId().equals(newActiveVersion.getId())) {
                        VariantVersionStatus oldStatus = currentActiveVersion.getStatus();

                        currentActiveVersion.changeStatus(VariantVersionStatus.ARCHIVED);
                        variantVersionRepository.save(currentActiveVersion);

                        lifecycleHistoryRepository.save(
                                new VariantVersionLifecycleHistory(
                                        currentActiveVersion,
                                        oldStatus,
                                        VariantVersionStatus.ARCHIVED,
                                        1L,
                                        "Automatically archived because another version became ACTIVE",
                                        true
                                )
                        );
                    }
                });
    }

    @Override
    public List<VariantVersionLifecycleHistoryResponseDTO> getHistory(Long variantVersionId) {
        return lifecycleHistoryRepository.findByVariantVersionIdWithRelations(variantVersionId)
                .stream()
                .map(VariantVersionLifecycleHistoryResponseDTO::fromEntity)
                .toList();
    }

    private Variant getVariant(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Variant not found"));
    }

    private VariantVersion getVariantVersion(Long id) {
        return variantVersionRepository.findById(id)
                .orElseThrow(() -> new PortfolioResourceNotFoundException("Variant version not found"));
    }
}