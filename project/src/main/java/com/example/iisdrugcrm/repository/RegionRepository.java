package com.example.iisdrugcrm.repository;

import com.example.iisdrugcrm.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}