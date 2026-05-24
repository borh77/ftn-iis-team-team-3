import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.token';
import { CreateUserPayload, SpringPage, UserRow } from './auth/auth.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);

  list(page = 0, size = 8): Observable<SpringPage<UserRow>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<SpringPage<UserRow>>(`${this.apiBaseUrl}/api/users`, { params });
  }

  create(payload: CreateUserPayload): Observable<UserRow> {
    return this.http.post<UserRow>(`${this.apiBaseUrl}/api/users`, payload);
  }
}
