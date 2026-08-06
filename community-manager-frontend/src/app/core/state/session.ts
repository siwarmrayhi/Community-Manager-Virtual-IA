import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'cm_user_email';

@Injectable({
  providedIn: 'root',
})
export class Session {
  readonly userEmail = signal<string | null>(localStorage.getItem(STORAGE_KEY));

  setUserEmail(email: string): void {
    this.userEmail.set(email);
    localStorage.setItem(STORAGE_KEY, email);
  }

  clear(): void {
    this.userEmail.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }
}
