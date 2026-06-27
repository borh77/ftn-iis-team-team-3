package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.order.ValidationResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface OrderValidationService {

    ValidationResultDTO validateOrderDocument(String username, MultipartFile file);
}
