package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.BuyerCatalogDTO;

public interface BuyerCatalogService {

    BuyerCatalogDTO getCatalogForBuyer(String username);
}
