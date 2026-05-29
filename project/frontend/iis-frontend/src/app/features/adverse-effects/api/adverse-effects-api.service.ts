import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/api.token';
import {
  AdverseEffectReport,
  CreateDoctorReportRequest,
  CreatePatientReportRequest
} from '../models/adverse-effect-report.model';

@Injectable({ providedIn: 'root' })
export class AdverseEffectsApiService {

  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private get base() { return `${this.apiBaseUrl}/api/adverse-effects`; }

  // US-01: Lekar kreira nalog
  createDoctorReport(dto: CreateDoctorReportRequest): Observable<AdverseEffectReport> {
    return this.http.post<AdverseEffectReport>(`${this.base}/doctor-reports`, dto);
  }

  // US-02: Pacijent kreira nalog
  createPatientReport(dto: CreatePatientReportRequest): Observable<AdverseEffectReport> {
    return this.http.post<AdverseEffectReport>(`${this.base}/patient-reports`, dto);
  }

  // US-03: Lekar vidi samo svoje naloge
  getMyReports(): Observable<AdverseEffectReport[]> {
    return this.http.get<AdverseEffectReport[]>(`${this.base}/my-reports`);
  }

  // US-04: Farmakovigilant vidi sve naloge
  getAllReports(): Observable<AdverseEffectReport[]> {
    return this.http.get<AdverseEffectReport[]>(this.base);
  }

  // Detalji jednog naloga
  getReportById(id: number): Observable<AdverseEffectReport> {
    return this.http.get<AdverseEffectReport>(`${this.base}/${id}`);
  }

  // US-03: Lekar edituje nalog (samo dok je SUBMITTED)
  updateDoctorReport(id: number, dto: any): Observable<AdverseEffectReport> {
    return this.http.put<AdverseEffectReport>(`${this.base}/doctor-reports/${id}`, dto);
  }
}
