package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.pricelist.PricelistActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PricelistActivityLogRepository extends JpaRepository<PricelistActivityLog, Long>, JpaSpecificationExecutor<PricelistActivityLog> {
}
