package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findTopByOrderByIdDesc();

    boolean existsByOfferId(Long offerId);
}