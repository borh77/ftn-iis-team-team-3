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
