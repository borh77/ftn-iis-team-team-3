package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MockCatalogService implements CatalogService {

    private static final List<CatalogVariantDTO> ACTIVE_VARIANTS = List.of(
            new CatalogVariantDTO(1001L, "Aspirin 100 mg", true),
            new CatalogVariantDTO(1002L, "Paracetamol 500 mg", true),
            new CatalogVariantDTO(1003L, "Ibuprofen 200 mg", true),
            new CatalogVariantDTO(1004L, "Amoxicillin 500 mg", true)
    );

    @Override
    public Map<Long, CatalogVariantDTO> findActiveVariantsByIds(Collection<Long> variantIds) {
        Set<Long> requestedIds = variantIds.stream().collect(Collectors.toSet());
        Map<Long, CatalogVariantDTO> matches = new LinkedHashMap<>();

        for (CatalogVariantDTO variant : ACTIVE_VARIANTS) {
            if (requestedIds.contains(variant.getId()) && variant.isActive()) {
                matches.put(variant.getId(), variant);
            }
        }

        return matches;
    }

    @Override
    public List<CatalogVariantDTO> getActiveVariants() {
        return ACTIVE_VARIANTS;
    }
}