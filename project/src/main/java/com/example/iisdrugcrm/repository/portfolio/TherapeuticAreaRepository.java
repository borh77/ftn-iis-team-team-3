package com.example.iisdrugcrm.repository.portfolio;

import com.example.iisdrugcrm.domain.portfolio.EntityStatus;
import com.example.iisdrugcrm.domain.portfolio.TherapeuticArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapeuticAreaRepository extends JpaRepository<TherapeuticArea, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<TherapeuticArea> findByStatus(EntityStatus status);
}
