package com.example.iisdrugcrm.domain.adverse;

public enum ReportStatus {
    SUBMITTED,    // Lekar kreirao nalog, čeka analizu
    UNDER_REVIEW, // Farmakovigilant preuzeo nalog (Sprint 2)
    CLOSED,       // Analiza završena (Sprint 2)
    EVIDENCED     // Pacijentov nalog — automatski terminalni status
}
