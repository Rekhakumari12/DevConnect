import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, of, Observable, tap, firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

export interface AuthUser {
  username: string;
}

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  readonly isAuthenticated$ = new BehaviorSubject<boolean>(false);
  readonly currentUser$ = new BehaviorSubject<AuthUser | null>(null);
  private initPromise: Promise<void> | null = null;

  constructor(private readonly authService: AuthService) {}

  initFromSession(): Promise<void> {
    // Return existing promise if already initializing
    if (this.initPromise) {
      return this.initPromise;
    }

    this.initPromise = firstValueFrom(
      this.authService.getMyProfile().pipe(
        tap((profile) => {
          if (profile) {
            this.isAuthenticated$.next(true);
            this.currentUser$.next({ username: profile.username });
          }
        }),
        catchError(() => {
          this.isAuthenticated$.next(false);
          this.currentUser$.next(null);
          return of(null);
        }),
      ),
    ).then(() => {
      // Promise resolves when initialization is complete
    });

    return this.initPromise;
  }

  clear(): void {
    this.isAuthenticated$.next(false);
    this.currentUser$.next(null);
  }

  setAuthenticated(username: string): void {
    this.isAuthenticated$.next(true);
    this.currentUser$.next({ username });
  }
}
