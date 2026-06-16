package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.CustomerNeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerNeedRepository extends JpaRepository<CustomerNeed, Long> {

    List<CustomerNeed> findByCustomerId(Long customerId);

    List<CustomerNeed> findBySalesProcessId(Long salesProcessId);
}