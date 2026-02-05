# Authentication (Registration, Login, Session Handling) Implementation Plan

## Overview

Implement end-to-end authentication for DevConnect using the existing stateless JWT backend but exposing it as a server-managed session via httpOnly cookies, with Angular registration and login flows, session persistence across reloads, and guarded signed-in-only views.

## Architecture

- Backend (Spring Boot):
  - `/auth/login` authenticates via username and issues a JWT set as an httpOnly cookie.
  - `/api/users/register` creates a new user, enforces validation, and also issues the JWT cookie for auto sign-in.
  - `/auth/logout` clears the JWT cookie and effectively ends the session.
  - `GET /api/users/my-profile` remains the canonical "who am I" endpoint to detect a valid session.
- Security:
  - `JwtFilter` reads the JWT from the httpOnly cookie (and optionally Authorization header for compatibility) and populates `SecurityContext`.
- Frontend (Angular):
  - `Register` and new `Login` components use a shared `AuthService` to call backend APIs.
  - App bootstraps by calling `GET /api/users/my-profile` to determine if a session exists and hydrate navbar/auth state.
  - Angular route guards (`canActivate`) protect signed-in-only routes like "My profile" and "My posts".

## Implementation Phases

### Phase 1: Backend Login Contract (Username Only)

**Files**:

- `dev_connect_backend/src/main/java/com/example/demo/dto/login/LoginRequest.java`
- `dev_connect_backend/src/main/java/com/example/demo/AuthService.java`

**Test Files**:

- `dev_connect_backend/src/test/java/com/example/demo/AuthServiceTest.java` (to be added)

Update the login DTO and auth flow so that a single username field is used for authentication, delegating directly to Spring Security.

**Key code changes:**

```java
// dto/login/LoginRequest.java
package com.example.demo.dto.login;

public record LoginRequest(
    String username,   // username
        String password
) { }
```

```java
// AuthService.java
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
             JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public String verify(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        return jwtUtil.generateToken(authentication.getName());
    }
}
```

Test cases for this phase:

- `AuthService.verify`:
  - With a valid username/password, it authenticates and returns a non-null JWT.
  - With invalid credentials, `AuthenticationManager` throws and the error propagates into the global exception handler.

Technical details and Assumptions:

- Authentication uses the canonical username only; email is not accepted as a login identifier.
- The existing `JwtUtil.generateToken(String subject)` is assumed to take the username as subject.
- No change to password hashing; reuse existing BCrypt configuration.

---

### Phase 2: Backend Session Semantics via httpOnly Cookie

**Files**:

- `dev_connect_backend/src/main/java/com/example/demo/controller/AuthController.java`
- `dev_connect_backend/src/main/java/com/example/demo/controller/UserController.java`
- `dev_connect_backend/src/main/java/com/example/demo/security/JwtFilter.java`
- `dev_connect_backend/src/main/java/com/example/demo/security/JwtUtil.java`
- `dev_connect_backend/src/main/java/com/example/demo/config/SecurityConfig.java`

**Test Files**:

- `dev_connect_backend/src/test/java/com/example/demo/controller/AuthControllerTest.java` (new)
- `dev_connect_backend/src/test/java/com/example/demo/controller/UserControllerAuthIntegrationTest.java` (new)
- `dev_connect_backend/src/test/java/com/example/demo/security/JwtFilterTest.java` (new or extended)

Change login and registration to issue the JWT as an httpOnly cookie (no token in JSON body) and add a logout endpoint that clears the cookie. Update the JWT filter to read the token from the cookie.

**Key code changes:**

```java
// controller/AuthController.java
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
                                               HttpServletResponse response) {
        String token = authService.verify(loginRequest);

        ResponseCookie cookie = ResponseCookie.from("DEVCONNECT_JWT", token)
                .httpOnly(true)
                .secure(false) // TODO: true behind HTTPS
                .sameSite("Lax")
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Do NOT return the token in the body; return basic user info / message instead
        return ResponseEntity.ok(new LoginResponse(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("DEVCONNECT_JWT", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }
}
```

```java
// controller/UserController.java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody UserProfileRequest user,
                                                        HttpServletResponse response) {
        UserProfileResponse created = userService.register(user);

        // Auto-login: issue JWT cookie based on created username
        String token = jwtUtil.generateToken(created.username());
        ResponseCookie cookie = ResponseCookie.from("DEVCONNECT_JWT", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

```java
// security/JwtFilter.java (only the token extraction part)

private String resolveToken(HttpServletRequest request) {
    // Prefer cookie-based token for session semantics
    if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
            if ("DEVCONNECT_JWT".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
    }

    // (Optional) Fallback to Authorization header for compatibility
    String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
    }
    return null;
}
```

Test cases for this phase:

- `AuthControllerTest`:
  - `/auth/login` with valid credentials returns 200 and sets a `DEVCONNECT_JWT` cookie with `HttpOnly` and `SameSite=Lax`.
  - `/auth/login` does NOT include a token in the JSON response body.
  - `/auth/logout` returns 204 and sets a `DEVCONNECT_JWT` cookie with `Max-Age=0`.
- `UserControllerAuthIntegrationTest`:
  - `/api/users/register` with valid payload returns 201, creates a user, and sets a `DEVCONNECT_JWT` cookie.
  - Following `GET /api/users/my-profile` with that cookie returns 200 and the created profile.
- `JwtFilterTest`:
  - When `DEVCONNECT_JWT` cookie is present and valid, `SecurityContext` is populated.
  - When cookie is missing or invalid, no authentication is set and downstream endpoints return 401 for protected routes.

Technical details and Assumptions:

- For local development, `secure(false)` is acceptable; production should set `secure(true)` and ensure HTTPS.
- The cookie name `DEVCONNECT_JWT` is a constant; consider moving it to a configuration class.
- `LoginResponse` may be adapted to carry only non-sensitive information (e.g., username) or even be an empty body depending on frontend needs.

---

### Phase 3: Angular AuthService, Registration Flow, and New Login Component

**Files**:

- `dev_connect_frontend/src/app/register/register.ts`
- `dev_connect_frontend/src/app/register/register.html`
- `dev_connect_frontend/src/app/app.routes.ts`
- `dev_connect_frontend/src/app/app.ts`
- `dev_connect_frontend/src/app/login/login.ts` (new)
- `dev_connect_frontend/src/app/login/login.html` (new)
- `dev_connect_frontend/src/app/common/form-field-errors/form-field-errors.ts`

**Test Files**:

- `dev_connect_frontend/src/app/register/register.spec.ts`
- `dev_connect_frontend/src/app/login/login.spec.ts` (new)
- `dev_connect_frontend/src/app/common/form-field-errors/form-field-errors.spec.ts`
- `dev_connect_frontend/src/app/app.spec.ts`

Introduce a frontend `AuthService` to centralize HTTP calls and wire up registration and login flows with proper validation and error handling.

**Key code changes:**

```typescript
// app/auth.service.ts (new)
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  skills: string[];
  bio?: string;
}

export interface LoginRequest {
  identifier: string; // username or email
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  register(payload: RegisterRequest): Observable<any> {
    return this.http.post('/api/users/register', payload, {
      withCredentials: true,
    });
  }

  login(payload: LoginRequest): Observable<any> {
    return this.http.post('/auth/login', payload, { withCredentials: true });
  }

  logout(): Observable<void> {
    return this.http.post<void>('/auth/logout', {}, { withCredentials: true });
  }

  getMyProfile(): Observable<any> {
    return this.http.get('/api/users/my-profile', { withCredentials: true });
  }
}
```

```typescript
// register/register.ts
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, FormFieldErrors],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerForm!: FormGroup;
  serverError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group(
      {
        username: ['', [Validators.required, Validators.minLength(4)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
        skills: ['', [Validators.required]], // at least one skill required
        bio: [''],
      },
      { validators: passwordMatcherValidator },
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
      bio: value.bio,
    };

    this.serverError = null;
    this.authService.register(payload).subscribe({
      next: () => {
        // Auto-signed in by cookie; navigate to public feed
        this.router.navigate(['/feed']);
      },
      error: (err) => {
        // Map backend validation errors into a general summary for now
        this.serverError =
          err.error?.message ??
          'Registration failed. Please check your details.';
      },
    });
  }
}
```

```typescript
// login/login.ts (new)
import { Component } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, LoginRequest } from '../auth.service';
import { FormFieldErrors } from '../common/form-field-errors/form-field-errors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, FormFieldErrors],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  loginForm!: FormGroup;
  authError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const payload: LoginRequest = this.loginForm.value;
    this.authError = null;

    this.authService.login(payload).subscribe({
      next: () => {
        this.router.navigate(['/feed']);
      },
      error: () => {
        this.authError =
          'Sign-in failed. Please check your credentials and try again.';
      },
    });
  }
}
```

Test cases for this phase:

- `Register` component:
  - Renders all required fields and displays client-side validation errors when fields are empty.
  - When the server returns a validation error (400), the error summary is displayed and form values remain.
  - On successful registration, `router.navigate(['/feed'])` is called.
- `Login` component:
  - Shows validation error when identifier or password are empty.
  - On 401/403 from backend, displays a non-technical error message, keeping form values.
- `AuthService`:
  - Each method calls the correct URL with `withCredentials: true`.

Technical details and Assumptions:

- `FormFieldErrors` component is used to show per-field validation; ensure its `@Input() control` wiring matches how `f.username` etc. are passed.
- `/feed` route will be added/confirmed in `app.routes.ts` in the next phase; it can initially point to a placeholder feed component.

---

### Phase 4: Frontend Session Handling, Navbar Integration, and Route Guards

**Files**:

- `dev_connect_frontend/src/app/app.ts`
- `dev_connect_frontend/src/app/app.html`
- `dev_connect_frontend/src/app/app.routes.ts`
- `dev_connect_frontend/src/app/navbar/navbar.ts`
- `dev_connect_frontend/src/app/navbar/navbar.html`
- `dev_connect_frontend/src/app/auth.guard.ts` (new)

**Test Files**:

- `dev_connect_frontend/src/app/navbar/navbar.spec.ts`
- `dev_connect_frontend/src/app/auth.guard.spec.ts` (new)
- `dev_connect_frontend/src/app/app.spec.ts`

Implement a lightweight auth state in the Angular app that is initialized from the backend on load, wire the navbar to that state, and add route guards for signed-in-only views.

**Key code changes:**

```typescript
// app/auth-state.service.ts (new)
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

  constructor(private authService: AuthService) {}

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
}
```

```typescript
// app/auth.guard.ts (new)
import { Injectable } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStateService } from './auth-state.service';
import { firstValueFrom } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivateFn {
  constructor(
    private authState: AuthStateService,
    private router: Router,
  ) {}

  async canActivate(): Promise<boolean> {
    // Ensure state is initialized once; in a simple app we can call init at bootstrap.
    const isAuthed = await firstValueFrom(this.authState.isAuthenticated$);
    if (!isAuthed) {
      this.router.navigate(['/login']);
      return false;
    }
    return true;
  }
}
```

```typescript
// app/app.ts
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  isUserAuthenticated = false;
  loggedInUsername: string | null = null;

  constructor(
    private authState: AuthStateService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.authState.isAuthenticated$.subscribe(
      (authed) => (this.isUserAuthenticated = authed),
    );
    this.authState.currentUser$.subscribe(
      (user) => (this.loggedInUsername = user?.username ?? null),
    );

    // Initialize from server-side session when app loads
    this.authState.initFromSession();
  }

  onUserLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.authState.clear();
      },
      error: () => {
        // Even if backend fails, clear local state to be safe
        this.authState.clear();
      },
    });
  }
}
```

```typescript
// app/app.routes.ts
import { Routes } from '@angular/router';
import { Register } from './register/register';
import { Login } from './login/login';
import { AuthGuard } from './auth.guard';
// import Feed, MyProfile, MyPosts components as they are implemented

export const routes: Routes = [
  { path: '', redirectTo: '/feed', pathMatch: 'full' },
  { path: 'feed' /* component: FeedComponent */ },
  { path: 'register', component: Register },
  { path: 'login', component: Login },
  {
    path: 'profile',
    /* component: ProfileComponent, */ canActivate: [AuthGuard],
  },
  {
    path: 'my-posts',
    /* component: MyPostsComponent, */ canActivate: [AuthGuard],
  },
];
```

```html
<!-- app/navbar/navbar.html (adjust bindings only) -->
<nav class="navbar">
  <div class="navbar-left">
    <a routerLink="/" class="navbar-brand">
      <span class="icon-code"></span> DevConnect
    </a>
  </div>

  <ng-container *ngIf="isAuthenticated; else guestLinks">
    <!-- existing authenticated navbar markup -->
  </ng-container>

  <ng-template #guestLinks>
    <div class="navbar-right">
      <a routerLink="/login" class="nav-link">Login</a>
      <a routerLink="/register" class="nav-link">Register</a>
    </div>
  </ng-template>
</nav>
```

Test cases for this phase:

- `AuthStateService`:
  - When `getMyProfile` returns 200, `isAuthenticated$` becomes true and `currentUser$` holds username.
  - When `getMyProfile` returns 401 or an error, both streams are reset to signed-out.
- `AuthGuard`:
  - When `isAuthenticated$` is false, navigation is prevented and router redirects to `/login`.
  - When `isAuthenticated$` is true, navigation to protected routes is allowed.
- `Navbar`:
  - Renders login/register links when `isAuthenticated` is false.
  - Renders user dropdown and logout option when `isAuthenticated` is true.
- `App` component:
  - Calls `authState.initFromSession()` on init and wires output streams into navbar inputs.

Technical details and Assumptions:

- For simplicity, `AuthGuard` uses current in-memory auth state; if needed, it can also trigger a fresh `getMyProfile` call when first used.
- The actual feed, profile, and my-posts components can be simple placeholders initially; the story concentrates on auth and session behavior.

---

### Phase 5: Error Handling, Session Expiry UX, and Test Hardening

**Files**:

- `dev_connect_backend/src/main/java/com/example/demo/exception/GlobalExceptionHandler.java`
- `dev_connect_backend/src/main/java/com/example/demo/config/SecurityConfig.java`
- `dev_connect_frontend/src/app/login/login.html`
- `dev_connect_frontend/src/app/register/register.html`
- `dev_connect_frontend/src/app/app.ts` (for session-expiry navigation tweaks if needed)

**Test Files**:

- Existing backend controller/security tests (extended for error paths)
- Angular specs for `Login`, `Register`, `AuthService`, `AuthStateService`

Polish the UX for error states, ensure session expiry flows match the story, and strengthen automated tests.

**Key code changes:**

```html
<!-- login/login.html -->
<div class="auth-card">
  <h2>Login</h2>

  <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
    <div class="form-group">
      <label for="identifier">Username or Email</label>
      <input id="identifier" formControlName="identifier" type="text" />
      <app-form-field-errors [control]="f.identifier"></app-form-field-errors>
    </div>

    <div class="form-group">
      <label for="password">Password</label>
      <input id="password" formControlName="password" type="password" />
      <app-form-field-errors [control]="f.password"></app-form-field-errors>
    </div>

    <div *ngIf="authError" class="error-summary">{{ authError }}</div>

    <button type="submit" class="btn btn-primary btn-full-width">Login</button>
  </form>

  <div class="auth-footer">
    New here? <a routerLink="/register" class="auth-link">Create an account</a>.
  </div>
</div>
```

```html
<!-- register/register.html (ensure form-field-errors wiring is correct) -->
<app-form-field-errors [control]="f.username"></app-form-field-errors>
<!-- similarly for email, password, confirmPassword, skills -->
<div *ngIf="serverError" class="error-summary">{{ serverError }}</div>
```

Test cases for this phase:

- Backend:
  - Global exception handler returns non-technical messages for invalid credentials and server errors.
  - 401/403 responses on protected endpoints when no/invalid cookie is present.
- Frontend:
  - On login 500 or network error, a friendly generic message is shown and form values remain.
  - When a signed-in session expires (backend starts returning 401 for `/api/users/my-profile`), `AuthStateService` resets and the navbar switches to signed-out state; attempting to visit `/profile` triggers redirect to `/login`.
  - Logout from navbar calls `/auth/logout`, clears state, and navigates to feed.

Technical details and Assumptions:

- For this story it is acceptable that, after being redirected to login from a protected route, a successful login always goes to the feed rather than back to the original deep link.
- Error messaging will use existing patterns from `GlobalExceptionHandler` without introducing a full i18n layer.

---

## Technical Considerations

- **Dependencies**:
  - Angular `HttpClientModule` must be imported in the app bootstrap if not already.
  - No new backend libraries are required; cookie handling uses Spring MVC / HttpServletResponse.
- **Edge Cases**:
  - Cookie tampering: invalid tokens should be rejected by `JwtFilter` and treated as signed-out.
  - Concurrent logins: issuing a new JWT cookie simply replaces the old one; no server-side session list is maintained.
  - Browser-level cookie rules: ensure SameSite and path settings work with the Angular app origin.
- **Testing Strategy**:
  - Unit tests for repository and service changes (username/email login).
  - Controller and filter integration tests covering login, register + auto login, logout, and protected endpoints.
  - Angular unit tests for components, services, and guards, focusing on validation, navigation, and error UX.
- **Performance**:
  - Auth flows are simple one-off HTTP calls; no special performance work is needed beyond standard best practices.
  - JWT parsing and cookie lookup in `JwtFilter` are O(1) for typical cookie counts.
- **Security**:
  - Storing JWT in an httpOnly cookie reduces exposure to XSS attacks.
  - Ensure CSRF risk is evaluated; for this API-oriented app with same-origin SPA and SameSite=Lax cookies, risk is reduced but should be documented.
  - Enforce strong password rules both on frontend (basic checks) and backend (authoritative validation).

## Testing Notes

- Each phase introduces new behavior that should be covered by tests in the same PR/commit as the implementation.
- Prefer focused tests with clear Arrange–Act–Assert sections over large, multi-assertion tests.
- Use existing backend testing patterns (JUnit 5, Mockito) and frontend patterns (Angular TestBed, Jasmine/Karma) already present in the repo.
- Validate both happy paths and error/edge conditions, especially around invalid credentials, expired/invalid cookies, and navigation when signed out.

## Success Criteria

- [ ] Users can register with username, email, password, and at least one skill, seeing clear client- and server-side validation messaging.
- [ ] After successful registration, the user is automatically signed in (cookie set) and redirected to the public feed.
- [ ] Users can log in with either username or email plus password, and a secure httpOnly JWT cookie is set on success.
- [ ] Refreshing the page while the JWT is valid keeps the user signed in; navbar and guarded routes behave accordingly.
- [ ] When the session expires or cookie is invalid, protected routes redirect to login and the navbar shows the signed-out state.
- [ ] Logout from the navbar clears the cookie on the backend, resets frontend state, and returns the user to a public view (feed).
