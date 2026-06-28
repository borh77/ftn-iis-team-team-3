package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.PricelistWizardStateDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistWizardSummaryDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveBasicInfoStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveItemsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveTeamAccessStepDTO;
import com.example.iisdrugcrm.dto.pricelist.SaveThresholdsStepDTO;
import com.example.iisdrugcrm.dto.pricelist.StartPricelistWizardResponseDTO;
import java.util.List;

public interface PricelistWizardService {

    StartPricelistWizardResponseDTO startWizard(Long currentUserId);

    List<PricelistWizardStateDTO> getDrafts(Long currentUserId);

    PricelistWizardStateDTO getWizardState(Long pricelistId, Long currentUserId);

    PricelistWizardStateDTO saveBasicInfo(Long pricelistId, SaveBasicInfoStepDTO dto, Long currentUserId);

    PricelistWizardStateDTO saveTeamAccess(Long pricelistId, SaveTeamAccessStepDTO dto, Long currentUserId);

    PricelistWizardStateDTO saveItems(Long pricelistId, SaveItemsStepDTO dto, Long currentUserId);

    PricelistWizardStateDTO saveThresholds(Long pricelistId, SaveThresholdsStepDTO dto, Long currentUserId);

    PricelistWizardSummaryDTO getSummary(Long pricelistId, Long currentUserId);

    PricelistWizardStateDTO finishWizard(Long pricelistId, Long currentUserId);
}
