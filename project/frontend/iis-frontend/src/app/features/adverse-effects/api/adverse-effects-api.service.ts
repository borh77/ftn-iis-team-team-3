import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api.token';
import {
  AdverseEffectReport,
  AnalystNote,
  ChangeStatusRequest,
  CreateDoctorReportRequest,
  CreatePatientReportRequest,
  StatusTransition
} from '../models/adverse-effect-report.model';

@Injectable({ providedIn: 'root' })
export class AdverseEffectsApiService {

  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private get base() { return `${this.apiBaseUrl}/api/adverse-effects`; }

  // US-01: Doctor creates a report
  createDoctorReport(dto: CreateDoctorReportRequest): Observable<AdverseEffectReport> {
    return this.http.post<AdverseEffectReport>(`${this.base}/doctor-reports`, dto);
  }

  // US-02: Patient creates a report
  createPatientReport(dto: CreatePatientReportRequest): Observable<AdverseEffectReport> {
    return this.http.post<AdverseEffectReport>(`${this.base}/patient-reports`, dto);
  }

  // US-03: Doctor sees only their reports
  getMyReports(): Observable<AdverseEffectReport[]> {
    return this.http.get<AdverseEffectReport[]>(`${this.base}/my-reports`);
  }

  // US-04: Pharmacovigilance user sees all reports
  getAllReports(): Observable<AdverseEffectReport[]> {
    return this.http.get<AdverseEffectReport[]>(this.base);
  }

  getAllReportsFiltered(filters: {
    status?: string;
    medicationName?: string;
    severity?: string;
  }): Observable<AdverseEffectReport[]> {
    const params: Record<string, string> = {};
    if (filters.status) params['status'] = filters.status;
    if (filters.medicationName) params['medicationName'] = filters.medicationName;
    if (filters.severity) params['severity'] = filters.severity;

    return this.http.get<AdverseEffectReport[]>(`${this.base}/reports`, { params });
  }

  // Single report details
  getReportById(id: number): Observable<AdverseEffectReport> {
    return this.http.get<AdverseEffectReport>(`${this.base}/${id}`);
  }

  // US-03: Doctor edits a report only while it is SUBMITTED
  updateDoctorReport(id: number, dto: any): Observable<AdverseEffectReport> {
    return this.http.put<AdverseEffectReport>(`${this.base}/doctor-reports/${id}`, dto);
  }

  changeStatus(id: number, dto: ChangeStatusRequest): Observable<AdverseEffectReport> {
    return this.http.put<AdverseEffectReport>(`${this.base}/reports/${id}/status`, dto);
  }

  addNote(id: number, dto: { content: string }): Observable<AnalystNote> {
    return this.http.post<AnalystNote>(`${this.base}/reports/${id}/notes`, dto);
  }

  getStatusHistory(id: number): Observable<StatusTransition[]> {
    return this.http.get<StatusTransition[]>(`${this.base}/reports/${id}/history`);
  }

  getNotes(id: number): Observable<AnalystNote[]> {
    return this.http.get<AnalystNote[]>(`${this.base}/reports/${id}/notes`);
  }
}
