import { AbstractControl, ValidatorFn } from '@angular/forms';

export const passwordMatcherValidator: ValidatorFn = (
  controls: AbstractControl
): { [key: string]: any} | null => {
  const password = controls.get('password');
  const confirmPassword = controls.get('confirmPassword');

  if (!password || !confirmPassword) null;

  if (password?.value !== confirmPassword?.value) {
    confirmPassword?.setErrors({ passwordMismatch: true });
    return { passwordMismatch: true }
  } else {
    if(confirmPassword?.hasError('passwordMismatch')) {
      confirmPassword.setErrors(null);
    }
  }

  return null
};
