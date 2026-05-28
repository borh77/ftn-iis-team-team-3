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

        for (Variant variant : variantRepository.searchVariants(null, null, false, EntityStatus.ACTIVE)) {
            if (requestedIds.contains(variant.getId())) {
                matches.put(variant.getId(), toCatalogVariantDTO(variant));
            }
        }

        return matches;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogVariantDTO> getActiveVariants() {
        return variantRepository.searchVariants(null, null, false, EntityStatus.ACTIVE).stream()
                .map(this::toCatalogVariantDTO)
                .toList();
    }

    private CatalogVariantDTO toCatalogVariantDTO(Variant variant) {
        return new CatalogVariantDTO(
                variant.getId(),
                variant.getProduct().getName() + " " + variant.getForm() + " " + variant.getDosage(),
                true
        );
    }
}