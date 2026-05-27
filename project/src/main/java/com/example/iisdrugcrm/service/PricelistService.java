package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.CreatePricelistDTO;
import com.example.iisdrugcrm.dto.pricelist.PricelistResponseDTO;
import java.util.List;

public interface PricelistService {

    PricelistResponseDTO createCenovnik(CreatePricelistDTO dto);

    List<PricelistResponseDTO> listCenovnici();
}