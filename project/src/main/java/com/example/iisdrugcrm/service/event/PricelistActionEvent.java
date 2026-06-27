package com.example.iisdrugcrm.service.event;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;

public record PricelistActionEvent(
        Long pricelistId,
        Long userId,
        Long teamId,
        PricelistActionType actionType,
        String description,
        PricelistStatus statusFrom,
        PricelistStatus statusTo
) {
    public PricelistActionEvent(
            Long pricelistId,
            Long userId,
            Long teamId,
            PricelistActionType actionType,
            String description
    ) {
        this(pricelistId, userId, teamId, actionType, description, null, null);
    }
}
