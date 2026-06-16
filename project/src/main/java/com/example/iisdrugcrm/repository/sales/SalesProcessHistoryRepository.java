package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.SalesProcessHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesProcessHistoryRepository extends JpaRepository<SalesProcessHistory, Long> {
}