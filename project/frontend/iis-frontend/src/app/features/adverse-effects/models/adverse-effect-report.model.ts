export type ReportStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'CLOSED' | 'EVIDENCED';
export type ReportType = 'DOCTOR' | 'PATIENT';

export interface AdverseEffectReport {
  id: number;
  createdAt: string;
  source: string;
  severity: string;
  symptomDate: string;
  status: ReportStatus;
  medicationName: string;
  reportType: ReportType;
  reporterUsername: string;
  currentVersionId?: number;
  currentVersionNumber?: number;
  // Doctor-specific
  effectDescription?: string;
  additionalNotes?: string;
  // Patient-specific
  symptoms?: string;
  additionalDesc?: string;
  // Shared patient info
  patientGender?: string;
  patientAge?: number;
}

export interface CreatePatientReportRequest {
  medicationName: string;
  symptoms: string;
  additionalDesc?: string;
  patientGender?: string;
  patientAge?: number;
  symptomDate: string;
}

export interface CreateDoctorReportRequest {
  medicationName: string;
  severity: string;
  source: string;
  symptomDate: string;
  effectDescription: string;
  additionalNotes?: string;
  patientGender?: string;
  patientAge?: number;
}

export interface StatusTransition {
  id: number;
  oldStatus: string;
  newStatus: string;
  changedAt: string;
  changedByUsername: string;
  comment?: string;
  priority?: string;
  closureReason?: string;
  verdict?: string;
}

export interface AdverseEffectReportVersion {
  id: number;
  reportId: number;
  versionNumber: number;
  active: boolean;
  createdAt: string;
  createdByUsername: string;
  medicationName: string;
  source?: string;
  severity?: string;
  symptomDate?: string;
  effectDescription?: string;
  additionalNotes?: string;
  patientGender?: string;
  patientAge?: number;
}

export interface AnalystNote {
  id: number;
  content: string;
  createdAt: string;
  authorUsername: string;
}

export interface ChangeStatusRequest {
  newStatus: ReportStatus;
  comment?: string;
  priority?: string;
  closureReason?: string;
  verdict?: string;
}

export interface AnalyticsCountItem {
  label: string;
  count: number;
  percentage: number;
}

export interface AnalyticsTimeBucket {
  period: string;
  count: number;
}

export interface AdverseEffectAnalyticsSummary {
  totalReports: number;
  doctorReports: number;
  patientReports: number;
  submittedReports: number;
  underReviewReports: number;
  closedReports: number;
  evidencedReports: number;
  reportsByMedication: AnalyticsCountItem[];
  reportsByEffect: AnalyticsCountItem[];
  reportsByStatus: AnalyticsCountItem[];
  reportsByReporterType: AnalyticsCountItem[];
  reportsOverTime: AnalyticsTimeBucket[];
}
