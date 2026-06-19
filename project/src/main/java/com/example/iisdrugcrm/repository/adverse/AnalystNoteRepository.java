package com.example.iisdrugcrm.repository.adverse;

import com.example.iisdrugcrm.domain.adverse.AnalystNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalystNoteRepository extends JpaRepository<AnalystNote, Long> {

    List<AnalystNote> findByReportIdOrderByCreatedAtAsc(Long reportId);
}

