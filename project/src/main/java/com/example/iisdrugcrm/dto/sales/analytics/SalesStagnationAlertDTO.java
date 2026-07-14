package com.example.iisdrugcrm.dto.sales.analytics;

import java.time.LocalDateTime;

public record SalesStagnationAlertDTO(
        Long id,
        Long salesProcessId,
        String processTitle,
        String stageName,
        String severity,
        Integer daysInStage,
        String message,
        String status,
        String followUpStatus,
        LocalDateTime createdAt
) {
}