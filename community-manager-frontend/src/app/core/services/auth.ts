import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest, AuthResponse } from '../models/auth.model';
import { Session } from '../state/session';

const TOKEN_KEY = 'cm_auth_token';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly session = inject(Session);
  private readonly baseUrl = environment.authUrl;

  readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  isAuthenticated(): boolean {
    return this.token() !== null;
  }

  register(payload: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, payload)
      .pipe(tap((response) => this.persistSession(response)));
  }

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, payload)
      .pipe(tap((response) => this.persistSession(response)));
  }

  requestPasswordReset(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/forgot-password`, { email });
  }

  logout(): void {
    this.token.set(null);
    localStorage.removeItem(TOKEN_KEY);
    this.session.clear();
  }

  private persistSession(response: AuthResponse): void {
    this.token.set(response.token);
    localStorage.setItem(TOKEN_KEY, response.token);
    this.session.setUserEmail(response.userEmail);
  }
}
