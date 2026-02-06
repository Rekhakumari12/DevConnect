import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
  APP_INITIALIZER,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './auth.interceptor';
import { AuthStateService } from './auth-state.service';

import { routes } from './app.routes';

export function initializeAuth(authState: AuthStateService) {
  return () => authState.initFromSession();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      deps: [AuthStateService],
      multi: true,
    },
  ],
};
