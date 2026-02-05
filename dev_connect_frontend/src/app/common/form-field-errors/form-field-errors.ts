import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

@Component({
  selector: 'app-form-field-errors',
  imports: [CommonModule],
  templateUrl: './form-field-errors.html',
  styleUrl: './form-field-errors.css',
})
export class FormFieldErrors {
  @Input() control!: AbstractControl | null;
}
