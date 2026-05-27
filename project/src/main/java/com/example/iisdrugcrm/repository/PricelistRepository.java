package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.pricelist.Pricelist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricelistRepository extends JpaRepository<Pricelist, Long> {

    List<Pricelist> findAllByOrderByIdDesc();
}