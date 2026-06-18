package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.SalesProcessHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesProcessHistoryRepository extends JpaRepository<SalesProcessHistory, Long> {

    List<SalesProcessHistory> findBySalesProcess_IdOrderByChangedAtDesc(Long salesProcessId);
}