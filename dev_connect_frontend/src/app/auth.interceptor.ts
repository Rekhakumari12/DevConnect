import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthStateService } from './auth-state.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authState = inject(AuthStateService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle 401 Unauthorized - session expired or invalid
      if (error.status === 401) {
        console.log('Session expired or unauthorized, clearing auth state');
        authState.clear();

        // Only redirect if not already on login or register pages
        const currentUrl = router.url;
        if (!currentUrl.includes('/login') && !currentUrl.includes('/register')) {
          router.navigate(['/login']);
        }
      }

      // Handle 403 Forbidden
      if (error.status === 403) {
        console.error('Access denied:', error.error?.message || 'Forbidden');
      }

      // Handle 500 Internal Server Error
      if (error.status === 500) {
        console.error('Server error:', error.error?.message || 'Internal server error');
      }

      return throwError(() => error);
    }),
  );
};
