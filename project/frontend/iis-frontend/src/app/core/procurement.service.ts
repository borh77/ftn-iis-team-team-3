import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { ValidationResult } from './procurement.models';

@Injectable({ providedIn: 'root' })
export class ProcurementService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  validateOrderDocument(file: File): Observable<ValidationResult> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ValidationResult>(`${this.apiBaseUrl}/api/procurement/validation`, formData);
  }
}
