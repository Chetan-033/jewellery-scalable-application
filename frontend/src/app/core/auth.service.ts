import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface UserInfo {
  id: string;
  username: string;
  displayName: string;
  roles: string[];
  permissions: string[];
}

interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInMinutes: number;
  user: UserInfo;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'jewellery_erp_token';
  private readonly userKey = 'jewellery_erp_user';

  readonly user = signal<UserInfo | null>(this.readUser());

  constructor(private readonly http: HttpClient, private readonly router: Router) {
    if (!this.isAuthenticated()) {
      this.clearSession();
    }
  }

  login(username: string, password: string) {
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, {
        username: username.trim(),
        password
      })
      .pipe(
        tap((res) => {
          localStorage.setItem(this.tokenKey, res.accessToken);
          localStorage.setItem(this.userKey, JSON.stringify(res.user));
          this.user.set(res.user);
        })
      );
  }

  /** Refresh user profile from API; clears session on 401 via interceptor. */
  refreshMe() {
    return this.http.get<UserInfo>(`${environment.apiBaseUrl}/auth/me`).pipe(
      tap((user) => {
        localStorage.setItem(this.userKey, JSON.stringify(user));
        this.user.set(user);
      })
    );
  }

  logout(returnUrl?: string | null): void {
    const hadToken = !!localStorage.getItem(this.tokenKey);
    if (hadToken) {
      this.http.post(`${environment.apiBaseUrl}/auth/logout`, {}).subscribe({ error: () => undefined });
    }
    this.clearSession();
    const extras =
      returnUrl && returnUrl !== '/login' && !returnUrl.startsWith('/login')
        ? { queryParams: { returnUrl } }
        : {};
    void this.router.navigate(['/login'], extras);
  }

  token(): string | null {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) {
      return null;
    }
    if (this.isJwtExpired(token)) {
      this.clearSession();
      return null;
    }
    return token;
  }

  isAuthenticated(): boolean {
    return !!this.token();
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.user.set(null);
  }

  private readUser(): UserInfo | null {
    const raw = localStorage.getItem(this.userKey);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as UserInfo;
    } catch {
      return null;
    }
  }

  private isJwtExpired(token: string): boolean {
    try {
      const parts = token.split('.');
      if (parts.length < 2) {
        return true;
      }
      const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
      if (!payload.exp) {
        return true;
      }
      // 30s skew so borderline tokens refresh via 401 handler cleanly
      return payload.exp * 1000 <= Date.now() + 30_000;
    } catch {
      return true;
    }
  }
}
