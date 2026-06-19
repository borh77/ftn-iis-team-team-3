package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.StatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusTransitionRepository extends JpaRepository<StatusTransition, Long> {

    List<StatusTransition> findByReportIdOrderByChangedAtAsc(Long reportId);
}

