package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.domain.PricelistStatus;
import com.example.iisdrugcrm.domain.pricelist.PricelistActionType;
import com.example.iisdrugcrm.dto.pricelist.PricelistDashboardSummaryDTO;
import java.time.OffsetDateTime;

public interface PricelistDashboardService {

    PricelistDashboardSummaryDTO getSummary(
            Long currentUserId,
            boolean admin,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Long teamId,
            Long regionId,
            PricelistStatus status,
            String customerSegment
    );
}
