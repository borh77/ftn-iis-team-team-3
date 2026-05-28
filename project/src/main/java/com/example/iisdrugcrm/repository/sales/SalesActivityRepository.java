package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.SalesActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesActivityRepository extends JpaRepository<SalesActivity, Long> {

    List<SalesActivity> findBySalesProcessId(Long salesProcessId);
}