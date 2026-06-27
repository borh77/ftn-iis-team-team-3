package com.example.iisdrugcrm.service.event;

import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;

public record PricelistActionEvent(
        Long pricelistId,
        Long userId,
        Long teamId,
        PricelistActionType actionType,
        String description
) {
}
