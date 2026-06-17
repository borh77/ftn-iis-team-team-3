import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { CreatePricelistPayload, Pricelist } from './pricelist.models';

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
}
