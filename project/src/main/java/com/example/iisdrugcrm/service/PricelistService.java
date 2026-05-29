package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import java.util.List;

public interface PricelistService {

    PricelistResponseDTO createCenovnik(CreatePricelistDTO dto, Long currentUserId);

    List<PricelistResponseDTO> listCenovnici();

    List<PricelistResponseDTO> listCenovniciForUser(Long currentUserId);
}