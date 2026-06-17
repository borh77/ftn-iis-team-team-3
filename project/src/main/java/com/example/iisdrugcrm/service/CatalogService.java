package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.CatalogVariantDTO;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CatalogService {

    Map<Long, CatalogVariantDTO> findActiveVariantsByIds(Collection<Long> variantIds);

    List<CatalogVariantDTO> getActiveVariants();
}