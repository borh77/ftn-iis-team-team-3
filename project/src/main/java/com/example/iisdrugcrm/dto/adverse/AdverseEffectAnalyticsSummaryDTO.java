package com.example.iisdrugcrm.dto.adverse;

import java.util.List;

public class AdverseEffectAnalyticsSummaryDTO {

    private long totalReports;
    private long doctorReports;
    private long patientReports;
    private long submittedReports;
    private long underReviewReports;
    private long closedReports;
    private long evidencedReports;

    private List<CountItemDTO> reportsByMedication;
    private List<CountItemDTO> reportsByEffect;
    private List<CountItemDTO> reportsByStatus;
    private List<CountItemDTO> reportsByReporterType;
    private List<TimeBucketDTO> reportsOverTime;

    public long getTotalReports() { return totalReports; }
    public void setTotalReports(long totalReports) { this.totalReports = totalReports; }

    public long getDoctorReports() { return doctorReports; }
    public void setDoctorReports(long doctorReports) { this.doctorReports = doctorReports; }

    public long getPatientReports() { return patientReports; }
    public void setPatientReports(long patientReports) { this.patientReports = patientReports; }

    public long getSubmittedReports() { return submittedReports; }
    public void setSubmittedReports(long submittedReports) { this.submittedReports = submittedReports; }

    public long getUnderReviewReports() { return underReviewReports; }
    public void setUnderReviewReports(long underReviewReports) { this.underReviewReports = underReviewReports; }

    public long getClosedReports() { return closedReports; }
    public void setClosedReports(long closedReports) { this.closedReports = closedReports; }

    public long getEvidencedReports() { return evidencedReports; }
    public void setEvidencedReports(long evidencedReports) { this.evidencedReports = evidencedReports; }

    public List<CountItemDTO> getReportsByMedication() { return reportsByMedication; }
    public void setReportsByMedication(List<CountItemDTO> reportsByMedication) { this.reportsByMedication = reportsByMedication; }

    public List<CountItemDTO> getReportsByEffect() { return reportsByEffect; }
    public void setReportsByEffect(List<CountItemDTO> reportsByEffect) { this.reportsByEffect = reportsByEffect; }

    public List<CountItemDTO> getReportsByStatus() { return reportsByStatus; }
    public void setReportsByStatus(List<CountItemDTO> reportsByStatus) { this.reportsByStatus = reportsByStatus; }

    public List<CountItemDTO> getReportsByReporterType() { return reportsByReporterType; }
    public void setReportsByReporterType(List<CountItemDTO> reportsByReporterType) { this.reportsByReporterType = reportsByReporterType; }

    public List<TimeBucketDTO> getReportsOverTime() { return reportsOverTime; }
    public void setReportsOverTime(List<TimeBucketDTO> reportsOverTime) { this.reportsOverTime = reportsOverTime; }

    public static class CountItemDTO {
        private String label;
        private long count;
        private double percentage;

        public CountItemDTO(String label, long count, double percentage) {
            this.label = label;
            this.count = count;
            this.percentage = percentage;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class TimeBucketDTO {
        private String period;
        private long count;

        public TimeBucketDTO(String period, long count) {
            this.period = period;
            this.count = count;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
