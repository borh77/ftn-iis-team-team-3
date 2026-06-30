import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { UserRole } from './auth/auth.models';

export interface AdminLookupOption {
  id: number;
  label: string;
}

export interface AdminUserLookupOption extends AdminLookupOption {
  displayName: string;
  email: string | null;
  role: UserRole;
}

export interface AdminFilterOptions {
  teams: AdminLookupOption[];
  users: AdminUserLookupOption[];
}

@Injectable({ providedIn: 'root' })
export class AdminFilterOptionsService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  getFilterOptions(): Observable<AdminFilterOptions> {
    return this.http.get<AdminFilterOptions>(`${this.apiBaseUrl}/api/admin/filter-options`);
  }
}
