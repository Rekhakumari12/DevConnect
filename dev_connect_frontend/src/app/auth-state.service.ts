import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, of } from 'rxjs';
import { AuthService } from './auth.service';

export interface AuthUser {
  username: string;
}

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  readonly isAuthenticated$ = new BehaviorSubject<boolean>(false);
  readonly currentUser$ = new BehaviorSubject<AuthUser | null>(null);

  constructor(private readonly authService: AuthService) {}

  initFromSession(): void {
    this.authService
      .getMyProfile()
      .pipe(
        catchError(() => {
          this.isAuthenticated$.next(false);
          this.currentUser$.next(null);
          return of(null);
        }),
      )
      .subscribe((profile) => {
        if (profile) {
          this.isAuthenticated$.next(true);
          this.currentUser$.next({ username: profile.username });
        }
      });
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
