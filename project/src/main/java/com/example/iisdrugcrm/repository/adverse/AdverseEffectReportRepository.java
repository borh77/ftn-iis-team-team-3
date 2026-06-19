package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.AdverseEffectReport;
import com.example.iisdrugcrm.domain.adverse.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AdverseEffectReportRepository extends JpaRepository<AdverseEffectReport, Long> {

    // Svi nalozi jednog korisnika (lekar vidi samo svoje)
    List<AdverseEffectReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    @Query("""
            select r
            from AdverseEffectReport r
            where (:status is null or r.status = :status)
              and lower(r.medicationName) like lower(concat('%', :medicationName, '%'))
              and (:severity is null or r.severity = :severity)
            order by r.createdAt desc
            """)
    List<AdverseEffectReport> findFiltered(
            @Param("status") ReportStatus status,
            @Param("medicationName") String medicationName,
            @Param("severity") String severity);
}
