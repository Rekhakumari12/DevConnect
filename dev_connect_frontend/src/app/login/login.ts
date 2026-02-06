import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { AuthStateService } from '../auth-state.service';
import { FormFieldErrors } from '../common/form-field-errors/form-field-errors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FormFieldErrors],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  loginForm: FormGroup;
  authError: string | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly authState: AuthStateService,
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.authError = null;

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: () => {
        console.log('Login successful, updating auth state');
        // Update auth state immediately so guard allows navigation
        this.authState.setAuthenticated(username);
        this.router.navigate(['/home']);
      },
      error: (err) => {
        console.error('Login failed:', err);
        if (err.status === 401) {
          this.authError = 'Invalid username or password';
        } else if (err.error?.message) {
          this.authError = err.error.message;
        } else {
          this.authError = 'An unexpected error occurred. Please try again.';
        }
      },
    });
  }
}
