package com.example.iisdrugcrm.dto.sales.analytics;

public record SalesStagnationThresholdDTO(
        String stageName,
        Integer warningDays,
        Integer criticalDays,
        Boolean active
) {
}