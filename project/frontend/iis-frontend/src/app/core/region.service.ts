import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from './api.token';
import { Observable } from 'rxjs';
import { Region, RegionPayload } from './region.model';

@Injectable({ providedIn: 'root' })
export class RegionService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  list(): Observable<Region[]> {
    return this.http.get<Region[]>(`${this.apiBaseUrl}/api/admin/regions`);
  }

  create(payload: RegionPayload): Observable<Region> {
    return this.http.post<Region>(`${this.apiBaseUrl}/api/admin/regions`, payload);
  }

  update(id: number, payload: RegionPayload): Observable<Region> {
    return this.http.put<Region>(`${this.apiBaseUrl}/api/admin/regions/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/api/admin/regions/${id}`);
  }
}