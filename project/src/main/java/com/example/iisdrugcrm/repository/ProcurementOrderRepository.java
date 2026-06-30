package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.procurement.ProcurementOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {

    List<ProcurementOrder> findAllByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    Optional<ProcurementOrder> findByIdAndBuyerId(Long id, Long buyerId);
}
