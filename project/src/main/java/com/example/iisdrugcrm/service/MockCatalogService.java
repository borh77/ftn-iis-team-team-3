package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.Variant;
import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import com.example.iisdrugcrm.repository.portfolio.VariantRepository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MockCatalogService implements CatalogService {

    private final VariantRepository variantRepository;

    public MockCatalogService(VariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, CatalogVariantDTO> findActiveVariantsByIds(Collection<Long> variantIds) {
        Set<Long> requestedIds = new LinkedHashSet<>(variantIds);
        Map<Long, CatalogVariantDTO> matches = new LinkedHashMap<>();

        // ISPRAVLJENO: Pravo ime metode i ispravan redosled parametara
        List<Variant> activeVariants = variantRepository.findByStatusWithRelations(
                EntityStatus.ACTIVE, // activeStatus
                false,               // includeArchived
                null,                // productId
                null                 // search
        );

        for (Variant variant : activeVariants) {
            if (requestedIds.contains(variant.getId())) {
                matches.put(variant.getId(), toCatalogVariantDTO(variant));
            }
        }

        return matches;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, CatalogVariantDTO> findVariantsByIdsIncludingInactive(Collection<Long> variantIds) {
        Set<Long> requestedIds = new LinkedHashSet<>(variantIds);
        if (requestedIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, CatalogVariantDTO> matches = new LinkedHashMap<>();
        List<Variant> variants = variantRepository.findByIdInWithRelationsIncludingReplacement(requestedIds);
        for (Variant variant : variants) {
            matches.put(variant.getId(), toCatalogVariantDTO(variant));
        }
        return matches;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogVariantDTO> getActiveVariants() {
        // ISPRAVLJENO: Pravo ime metode i ispravan redosled parametara
        return variantRepository.findByStatusWithRelations(
                EntityStatus.ACTIVE, // activeStatus
                false,               // includeArchived
                null,                // productId
                null                 // search
        ).stream()
                .map(this::toCatalogVariantDTO)
                .toList();
    }

    private CatalogVariantDTO toCatalogVariantDTO(Variant variant) {
        Variant replacement = variant.getReplacementVariant();
        return new CatalogVariantDTO(
                variant.getId(),
                variant.getProduct().getName() + " " + variant.getForm() + " " + variant.getDosage(),
                variant.getStatus() == EntityStatus.ACTIVE,
                replacement == null ? null : replacement.getId(),
                replacement == null ? null : replacement.getProduct().getName() + " " + replacement.getForm() + " " + replacement.getDosage()
        );
    }
}
