import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { passwordMatcherValidator } from '../validators/password-validator';
import { FormFieldErrors } from '../common/form-field-errors/form-field-errors';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, FormFieldErrors],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerForm!: FormGroup;

  // Dependency Injection:
  // - FormBuilder: A service that provides convenience methods for generating controls.
  // - Router: Used for navigating the user after successful registration.
  constructor(private fb: FormBuilder, private router: Router) {}

  ngOnInit(): void {
    this.fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(4)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
        skills: [''],
        bio: [''],
      },
      {
        //Group Validator: Applied to the FormGroup to check cross-field consistency
        validators: passwordMatcherValidator,
      }
    );
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit() {
    console.log('object');
  }
}
