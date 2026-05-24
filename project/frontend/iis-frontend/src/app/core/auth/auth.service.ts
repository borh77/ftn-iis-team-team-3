import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { API_BASE_URL } from '../api.token';
import { AuthSession, LoginRequest, LoginResponse, UserRole } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'iis-drug-crm.session';
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);
  private readonly sessionSubject = new BehaviorSubject<AuthSession | null>(this.restoreSession());

  readonly session$ = this.sessionSubject.asObservable();

  login(request: LoginRequest): Observable<AuthSession> {
    return this.http.post<LoginResponse>(`${this.apiBaseUrl}/api/auth/login`, request).pipe(
      map((response) => {
        const session: AuthSession = {
          token: response.token,
          username: response.username,
          roles: response.roles,
          active: response.active,
          hasChangedPassword: response.hasChangedPassword,
        };
        this.saveSession(session);
        return session;
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    this.sessionSubject.next(null);
  }

  get token(): string | null {
    return this.sessionSubject.value?.token ?? null;
  }

  isLoggedIn(): boolean {
    return Boolean(this.token);
  }

  hasRole(role: UserRole): boolean {
    return this.sessionSubject.value?.roles.includes(role) ?? false;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    return roles.length === 0 || roles.some((role) => this.hasRole(role));
  }

  resolveRedirectPath(session: AuthSession | null): string {
    if (!session) {
      return '/login';
    }

    if (session.roles.includes('ROLE_ADMIN')) {
      return '/admin/users';
    }

    if (session.roles.includes('ROLE_PRICELIST_CREATOR')) {
      return '/content';
    }

    if (session.roles.includes('ROLE_BUYER')) {
      return '/published-pricelists';
    }

    return '/login';
  }

  private saveSession(session: AuthSession): void {
    localStorage.setItem(this.storageKey, JSON.stringify(session));
    this.sessionSubject.next(session);
  }

  private restoreSession(): AuthSession | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      return null;
    }
  }
}
