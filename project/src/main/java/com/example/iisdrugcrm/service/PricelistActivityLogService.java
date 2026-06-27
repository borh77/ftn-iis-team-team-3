package com.example.iisdrugcrm.service;

import com.example.iisdrugcrm.dto.pricelist.PricelistActivityLogResponseDTO;
import com.example.iisdrugcrm.dto.pricelist.TeamPerformanceReportDTO;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PricelistActivityLogService {

    Page<PricelistActivityLogResponseDTO> findLogs(Long teamId, Long userId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    TeamPerformanceReportDTO getPerformanceReport(Long teamId, OffsetDateTime start, OffsetDateTime end);
}
