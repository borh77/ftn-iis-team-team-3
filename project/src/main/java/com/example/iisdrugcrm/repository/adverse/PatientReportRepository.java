package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.PatientReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientReportRepository extends JpaRepository<PatientReport, Long> {
}
