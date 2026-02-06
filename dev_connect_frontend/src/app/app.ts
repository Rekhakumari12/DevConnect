import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Navbar } from './navbar/navbar';
import { AuthStateService } from './auth-state.service';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  isUserAuthenticated: boolean = false;
  loggedInUsername: string | null = null;

  constructor(
    private readonly authState: AuthStateService,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    // Subscribe to auth state changes
    this.authState.isAuthenticated$.subscribe((authed) => (this.isUserAuthenticated = authed));
    this.authState.currentUser$.subscribe(
      (user) => (this.loggedInUsername = user?.username ?? null),
    );

    // Initialize auth state from server-side session when app loads
    this.authState.initFromSession();
  }

  onUserLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.authState.clear();
        this.router.navigate(['/login']);
      },
      error: () => {
        // Even if backend fails, clear local state to be safe
        this.authState.clear();
        this.router.navigate(['/login']);
      },
    });
  }
}
