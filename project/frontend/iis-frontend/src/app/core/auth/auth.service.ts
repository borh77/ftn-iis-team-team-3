import { isPlatformBrowser } from '@angular/common';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { API_BASE_URL } from '../api.token';
import { AuthSession, JwtPayload, LoginRequest, LoginResponse, UserRole } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'iis-drug-crm.session';
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly sessionSubject = new BehaviorSubject<AuthSession | null>(this.restoreSession());

  readonly session$ = this.sessionSubject.asObservable();

  get currentSession(): AuthSession | null {
    return this.sessionSubject.value;
  }

  requiresPasswordChange(): boolean {
    return !this.currentSession?.hasChangedPassword;
  }

  login(request: LoginRequest): Observable<AuthSession> {
    return this.http.post<LoginResponse>(`${this.apiBaseUrl}/api/auth/login`, request).pipe(
      map((response) => this.applySessionResponse(response)),
    );
  }

  applySessionResponse(response: LoginResponse): AuthSession {
    const session: AuthSession = {
      token: response.token,
      username: response.username,
      roles: response.roles,
      active: response.active,
      hasChangedPassword: response.hasChangedPassword,
    };
    this.saveSession(session);
    return session;
  }

  logout(): void {
    const token = this.token;

    if (token) {
      this.http.post<void>(`${this.apiBaseUrl}/api/auth/logout`, {}).subscribe({
        error: () => undefined,
      });
    }

    this.clearSession();
  }

  clearSession(): void {
    if (this.isBrowser()) {
      localStorage.removeItem(this.storageKey);
    }
    this.sessionSubject.next(null);
  }

  get token(): string | null {
    const session = this.sessionSubject.value;
    if (!session) {
      return null;
    }

    if (this.isSessionExpired(session.token)) {
      this.clearSession();
      return null;
    }

    return session.token;
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

    if (!session.hasChangedPassword) {
      return '/force-password-change';
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

    if (
      session.roles.includes('ROLE_SALES_REPRESENTATIVE') ||
      session.roles.includes('ROLE_ACCOUNT_MANAGER') ||
      session.roles.includes('ROLE_SALES_MANAGER')
    ) {
      return '/sales';
    }

    return '/login';
  }

  private saveSession(session: AuthSession): void {
    if (this.isBrowser()) {
      localStorage.setItem(this.storageKey, JSON.stringify(session));
    }
    this.sessionSubject.next(session);
  }

  private restoreSession(): AuthSession | null {
    if (!this.isBrowser()) {
      return null;
    }

    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      const session = JSON.parse(raw) as AuthSession;
      if (!Object.prototype.hasOwnProperty.call(session, 'hasChangedPassword')) {
        session.hasChangedPassword = this.decodeToken(session.token).hasChangedPassword ?? true;
      }
      return this.isSessionExpired(session.token) ? null : session;
    } catch {
      return null;
    }
  }

  private isSessionExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      return payload.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }

  private decodeToken(token: string): JwtPayload {
    const payloadBase64 = token.split('.')[1];
    const normalizedPayload = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
    const decodedPayload = atob(normalizedPayload);
    return JSON.parse(decodedPayload) as JwtPayload;
  }

  getSessionFromToken(token: string): AuthSession | null {
    try {
      const payload = this.decodeToken(token);
      return {
        token,
        username: payload.sub,
        roles: payload.roles,
        active: true,
        hasChangedPassword: payload.hasChangedPassword ?? true,
      };
    } catch {
      return null;
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
