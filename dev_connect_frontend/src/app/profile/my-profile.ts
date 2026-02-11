import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { FormFieldErrors } from '../common/form-field-errors/form-field-errors';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormFieldErrors],
  templateUrl: './my-profile.html',
  styleUrl: './my-profile.css',
})
export class MyProfileComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  serverError: string | null = null;
  successMessage: string | null = null;
  showEmail = false;
  private initialValue: any = { skills: '', bio: '' };

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      username: [{ value: '', disabled: true }],
      email: [{ value: '', disabled: true }],
      skills: ['', [Validators.required]],
      bio: ['', [Validators.maxLength(500)]],
      showEmailPublicly: [false],
    });

    this.loadProfile();
  }

  private loadProfile(): void {
    this.isLoading = true;
    this.serverError = null;

    this.authService.getMyProfile().subscribe({
      next: (profile) => {
        this.isLoading = false;
        this.form.patchValue({
          username: profile.username,
          email: profile.email,
          skills: profile.skills.join(', '),
          bio: profile.bio ?? '',
          showEmailPublicly: profile.showEmailPublicly ?? false,
        });
        this.initialValue = this.form.getRawValue();
        this.form.markAsPristine();
      },
      error: () => {
        this.isLoading = false;
        this.serverError = 'Unable to load your profile. Please try again.';
      },
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const skills = value.skills
      .split(',')
      .map((s: string) => s.trim())
      .filter((s: string) => !!s);

    if (skills.length === 0) {
      this.serverError = 'Please provide at least one skill.';
      return;
    }

    this.isLoading = true;
    this.serverError = null;
    this.successMessage = null;

    this.authService
      .updateMyProfile({
        skills,
        bio: value.bio || undefined,
        showEmailPublicly: value.showEmailPublicly,
      })
      .subscribe({
        next: (updated) => {
          this.isLoading = false;
          this.successMessage = 'Profile updated successfully!';
          this.form.patchValue({
            skills: updated.skills.join(', '),
            bio: updated.bio ?? '',
            showEmailPublicly: updated.showEmailPublicly,
          });
        },
        error: (err) => {
          this.isLoading = false;
          if (err.status === 0) {
            this.serverError = 'Unable to connect to server. Please check your connection.';
          } else if (err.status === 401) {
            this.serverError = 'Your session has expired. Please log in again.';
          } else if (err.status === 400) {
            this.serverError =
              err.error?.message || 'Invalid profile data. Please check your inputs.';
          } else if (err.status === 500) {
            this.serverError = 'Server error. Please try again later.';
          } else {
            this.serverError = err.error?.message || 'Could not update profile. Please try again.';
          }
        },
      });
  }

  toggleEmailVisibility(): void {
    this.showEmail = !this.showEmail;
  }

  hasChanges(): boolean {
    const normalize = (v: any) => ({
      ...v,
      skills: v.skills
        ?.split(',')
        .map((s: string) => s.trim())
        .filter(Boolean)
        .join(', '),
      bio: v.bio?.trim() || '',
    });

    const current = normalize(this.form.getRawValue());
    const initial = normalize(this.initialValue);

    return (
      current.skills !== initial.skills ||
      current.bio !== initial.bio ||
      current.showEmailPublicly !== initial.showEmailPublicly
    );
  }

  onCancel(): void {
    this.form.patchValue(this.initialValue);
    this.form.markAsPristine();
    this.serverError = null;
    this.successMessage = null;
  }
}
