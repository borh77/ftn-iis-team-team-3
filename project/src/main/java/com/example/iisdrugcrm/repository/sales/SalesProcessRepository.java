package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.SalesProcess;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesProcessRepository extends JpaRepository<SalesProcess, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    List<SalesProcess> findAll();
}