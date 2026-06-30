import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import {
  PricelistWizardState,
  PricelistWizardSummary,
  SaveBasicInfoStepRequest,
  SaveItemsStepRequest,
  SaveTeamAccessStepRequest,
  SaveThresholdsStepRequest,
  StartPricelistWizardResponse,
} from './pricelist.models';

@Injectable({ providedIn: 'root' })
export class PricelistWizardService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);
  private readonly wizardUrl = `${this.apiBaseUrl}/api/cenovnici`;

  startWizard(): Observable<StartPricelistWizardResponse> {
    return this.http.post<StartPricelistWizardResponse>(`${this.wizardUrl}/wizard`, {});
  }

  getDrafts(): Observable<PricelistWizardState[]> {
    return this.http.get<PricelistWizardState[]>(`${this.wizardUrl}/wizard/drafts`);
  }

  getWizardState(id: number): Observable<PricelistWizardState> {
    return this.http.get<PricelistWizardState>(`${this.wizardUrl}/${id}/wizard`);
  }

  saveBasicInfo(id: number, payload: SaveBasicInfoStepRequest): Observable<PricelistWizardState> {
    return this.http.put<PricelistWizardState>(`${this.wizardUrl}/${id}/wizard/basic-info`, payload);
  }

  saveTeamAccess(id: number, payload: SaveTeamAccessStepRequest): Observable<PricelistWizardState> {
    return this.http.put<PricelistWizardState>(`${this.wizardUrl}/${id}/wizard/team-access`, payload);
  }

  saveItems(id: number, payload: SaveItemsStepRequest): Observable<PricelistWizardState> {
    return this.http.put<PricelistWizardState>(`${this.wizardUrl}/${id}/wizard/items`, payload);
  }

  saveThresholds(id: number, payload: SaveThresholdsStepRequest): Observable<PricelistWizardState> {
    return this.http.put<PricelistWizardState>(`${this.wizardUrl}/${id}/wizard/thresholds`, payload);
  }

  getSummary(id: number): Observable<PricelistWizardSummary> {
    return this.http.get<PricelistWizardSummary>(`${this.wizardUrl}/${id}/wizard/summary`);
  }

  finishWizard(id: number): Observable<PricelistWizardState> {
    return this.http.post<PricelistWizardState>(`${this.wizardUrl}/${id}/wizard/finish`, {});
  }
}
