package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.CustomerCommunication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerCommunicationRepository extends JpaRepository<CustomerCommunication, Long> {
    List<CustomerCommunication> findByCustomerId(Long customerId);
}