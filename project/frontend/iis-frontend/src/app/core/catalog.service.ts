import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { CatalogVariant } from './catalog.model';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  listVariants(): Observable<CatalogVariant[]> {
    return this.http.get<CatalogVariant[]>(`${this.apiBaseUrl}/api/catalog/variants`);
  }
}
