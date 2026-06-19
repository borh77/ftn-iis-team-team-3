package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.CreateSpecialOfferDTO;
import com.example.iisdrugcrm.dto.pricelist.SpecialOfferResponseDTO;
import java.util.List;

public interface SpecialOfferService {
    SpecialOfferResponseDTO createOffer(CreateSpecialOfferDTO dto, Long currentUserId);
    List<SpecialOfferResponseDTO> listOffersForPricelist(Long pricelistId, Long currentUserId);
    SpecialOfferResponseDTO activateOffer(Long id, Long currentUserId);
    SpecialOfferResponseDTO archiveOffer(Long id, Long currentUserId);
}
