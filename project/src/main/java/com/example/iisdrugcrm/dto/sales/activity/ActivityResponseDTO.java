package com.example.iisdrugcrm.dto.sales.activity;

import com.example.iisdrugcrm.domain.sales.ActivityStatus;
import com.example.iisdrugcrm.domain.sales.ActivityType;

import java.time.LocalDateTime;

public record ActivityResponseDTO(
        Long id,
        ActivityType type,
        ActivityStatus status,
        String title,
        String description,
        LocalDateTime scheduledAt,
        LocalDateTime completedAt
) {
}