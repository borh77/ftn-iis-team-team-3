import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { ChangePricelistStatusPayload, CreatePricelistPayload, Pricelist } from './pricelist.models';

@Injectable({ providedIn: 'root' })
export class PricelistService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  create(payload: CreatePricelistPayload): Observable<Pricelist> {
    return this.http.post<Pricelist>(`${this.apiBaseUrl}/api/pricelists`, payload);
  }

  list(): Observable<Pricelist[]> {
    return this.http.get<Pricelist[]>(`${this.apiBaseUrl}/api/pricelists`);
  }

  mine(): Observable<Pricelist[]> {
    return this.http.get<Pricelist[]>(`${this.apiBaseUrl}/api/pricelists/mine`);
  }

  changeStatus(id: number, payload: ChangePricelistStatusPayload): Observable<Pricelist> {
    return this.http.put<Pricelist>(`${this.apiBaseUrl}/api/pricelists/${id}/status`, payload);
  }

  createNewVersion(id: number): Observable<Pricelist> {
    return this.http.post<Pricelist>(`${this.apiBaseUrl}/api/pricelists/${id}/versions`, {});
  }
}
