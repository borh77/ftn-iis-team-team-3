package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.DoctorReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorReportRepository extends JpaRepository<DoctorReport, Long> {
}
