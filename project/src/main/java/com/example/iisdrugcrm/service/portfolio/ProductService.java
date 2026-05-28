package com.example.iisdrugcrm.service.portfolio;

import com.example.iisdrugcrm.dto.portfolio.ProductRequestDTO;
import com.example.iisdrugcrm.dto.portfolio.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    List<ProductResponseDTO> getProducts(
        String search,
        Long subcategoryId,
        Long therapeuticAreaId,
        boolean includeArchived
    );

    ProductResponseDTO create(ProductRequestDTO dto);

    ProductResponseDTO update(Long id, ProductRequestDTO dto);

    void archive(Long id);
}