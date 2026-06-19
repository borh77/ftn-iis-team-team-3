import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { BuyerCatalog } from './buyer-catalog.models';

@Injectable({ providedIn: 'root' })
export class BuyerCatalogService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  getCatalog(): Observable<BuyerCatalog> {
    return this.http.get<BuyerCatalog>(`${this.apiBaseUrl}/api/catalog/buyer`);
  }
}
