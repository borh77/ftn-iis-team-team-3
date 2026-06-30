package com.example.iisdrugcrm.domain.adverse;

public enum ReportStatus {
    SUBMITTED,    // Doctor created the report, waiting for analysis
    UNDER_REVIEW, // Pharmacovigilance user took the report
    CLOSED,       // Analysis completed
    EVIDENCED     // Patient report, automatic terminal status
}
