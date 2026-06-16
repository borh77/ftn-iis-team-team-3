package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.OfferItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferItemRepository extends JpaRepository<OfferItem, Long> {
}