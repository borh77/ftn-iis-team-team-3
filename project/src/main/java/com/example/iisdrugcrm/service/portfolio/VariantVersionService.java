package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.domain.portfolio.VariantVersionStatus;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionResponseDTO;
import com.example.iisdrugcrm.dto.portfolio.VariantVersionStatusRequestDTO;

import com.example.iisdrugcrm.dto.portfolio.VariantVersionLifecycleHistoryResponseDTO;

import java.util.List;

public interface VariantVersionService {

    List<VariantVersionResponseDTO> getVariantVersions(
            String search,
            Long variantId,
            VariantVersionStatus status
    );

    VariantVersionResponseDTO create(VariantVersionRequestDTO dto);

    VariantVersionResponseDTO update(Long id, VariantVersionRequestDTO dto);

    VariantVersionResponseDTO changeStatus(Long id, VariantVersionStatusRequestDTO dto);

    List<VariantVersionLifecycleHistoryResponseDTO> getHistory(Long variantVersionId);
}