package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    boolean existsByEmail(String email);
}