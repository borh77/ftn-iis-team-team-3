package com.example.iisdrugcrm.repository.sales;

import com.example.iisdrugcrm.domain.sales.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findTopByOrderByIdDesc();
}