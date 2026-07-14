package com.example.iisdrugcrm.dto.sales.analytics;

import java.time.LocalDateTime;

public record StagnationCheckResultDTO(
        Integer checkedProcesses,
        Integer newAlerts,
        Integer newActivities,
        Integer openAlerts,
        LocalDateTime executedAt
) {
}