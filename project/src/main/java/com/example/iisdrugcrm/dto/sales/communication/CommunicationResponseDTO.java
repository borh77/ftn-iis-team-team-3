package com.example.iisdrugcrm.dto.sales.communication;

import com.example.iisdrugcrm.domain.sales.CommunicationType;

import java.time.LocalDateTime;

public record CommunicationResponseDTO(
        Long id,
        Long customerId,
        String customerName,
        CommunicationType type,
        LocalDateTime communicationDate,
        String summary
) {
}