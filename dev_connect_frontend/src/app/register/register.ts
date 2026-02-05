import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { passwordMatcherValidator } from '../validators/password-validator';
import { FormFieldErrors } from '../common/form-field-errors/form-field-errors';
import { AuthService, RegisterRequest } from '../auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, FormFieldErrors, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register implements OnInit {
  registerForm!: FormGroup;
  serverError: string | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(4)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
        skills: ['', [Validators.required]],
        bio: [''],
      },
      {
        validators: passwordMatcherValidator,
      },
    );
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const value = this.registerForm.value;
    const payload: RegisterRequest = {
      username: value.username,
      email: value.email,
      password: value.password,
      skills: value.skills
        .split(',')
        .map((s: string) => s.trim())
        .filter((s: string) => !!s),
      bio: value.bio || undefined,
    };

    this.serverError = null;
    this.authService.register(payload).subscribe({
      next: () => {
        // Auto-signed in by cookie; navigate to home
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.serverError = err.error?.message || 'Registration failed. Please check your details.';
      },
    });
  }
}
