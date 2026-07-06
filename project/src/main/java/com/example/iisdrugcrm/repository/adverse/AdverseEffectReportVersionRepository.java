package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.AdverseEffectReportVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdverseEffectReportVersionRepository extends JpaRepository<AdverseEffectReportVersion, Long> {

    List<AdverseEffectReportVersion> findByReportIdOrderByVersionNumberDesc(Long reportId);

    List<AdverseEffectReportVersion> findByReportIdAndActiveTrue(Long reportId);

    @Query("""
            select coalesce(max(v.versionNumber), 0)
            from AdverseEffectReportVersion v
            where v.report.id = :reportId
            """)
    Integer findMaxVersionNumberByReportId(@Param("reportId") Long reportId);
}
