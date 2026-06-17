package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.ChangePricelistStatusDTO;
import java.util.List;

public interface PricelistService {

    PricelistResponseDTO createCenovnik(CreatePricelistDTO dto, Long currentUserId);

    List<PricelistResponseDTO> listCenovnici();

    List<PricelistResponseDTO> listCenovniciForUser(Long currentUserId);

    List<PricelistResponseDTO> listTeamCenovniciForUser(Long currentUserId);

    PricelistResponseDTO changeStatus(Long id, ChangePricelistStatusDTO dto);

    PricelistResponseDTO changeStatus(Long id, ChangePricelistStatusDTO dto, Long currentUserId);

    PricelistResponseDTO createNewVersion(Long sourcePricelistId, Long currentUserId);

    PricelistResponseDTO replaceItemVariant(Long pricelistId, Long itemId, Long replacementVariantId, Long currentUserId);
}
