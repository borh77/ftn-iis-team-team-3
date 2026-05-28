package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.AdverseEffectReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdverseEffectReportRepository extends JpaRepository<AdverseEffectReport, Long> {

    // Svi nalozi jednog korisnika (lekar vidi samo svoje)
    List<AdverseEffectReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
