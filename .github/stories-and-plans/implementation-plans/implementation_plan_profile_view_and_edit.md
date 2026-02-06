# Profile View & Edit Implementation Plan

## Overview

Implement full-stack support for viewing and editing the signed-in developers profile (skills and bio only) and viewing other developers public profiles by username, while keeping email private except for the profile owner.

## Architecture

- **Backend (Spring Boot)**
  - Reuse existing profile endpoints under `/api/users`.
  - Harden `UserService` and DTOs so that:
    - `/api/users/my-profile` returns full profile for the authenticated user.
    - `/api/users?username=` returns a **public** profile view that never exposes email.
    - Profile updates via `/api/users/my-profile` only allow modifying `skills` and `bio`.
- **Frontend (Angular)**
  - Extend `AuthService` (or introduce a dedicated `ProfileService`) to call:
    - `GET /api/users/my-profile` (owner profile)
    - `PUT /api/users/my-profile` (update skills/bio)
    - `GET /api/users?username=` (public profile lookup)
  - Add two new screens:
    - **My Profile**: editable skills & bio; read-only username/email; owner-only email visibility toggle.
    - **Public Profile**: read-only username, skills, bio for any user by username.
  - Update navbar to expose "My profile" and a simple "Find profile by username" lookup.

## Implementation Phases

### Phase 1: Backend Profile Domain Rules & DTOs

**Files**: `dev_connect_backend/src/main/java/com/example/demo/UserService.java`,
`dev_connect_backend/src/main/java/com/example/demo/controller/UserController.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/profile/UserProfileResponse.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/profile/PublicUserProfileResponse.java` (new)

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/service/UserServiceTest.java`,
`dev_connect_backend/src/test/java/com/example/demo/controller/UserControllerTest.java`

Goal: Align backend behavior with story rules: only skills/bio editable; email never exposed on public profile; owner profile remains fully detailed.

**Key code changes:**

```java
// Example: introduce separate DTO for public profiles
public record PublicUserProfileResponse(
    UUID id,
    String username,
    List<String> skills,
    String bio
) {
    public PublicUserProfileResponse(User user) {
        this(user.getId(), user.getUsername(), user.getSkills(), user.getBio());
    }
}

// In UserService
public UserProfileResponse getProfileByUsernameForOwner(String username) {
    User user = getByUsername(username);
    return new UserProfileResponse(user); // includes email
}

public PublicUserProfileResponse getPublicProfileByUsername(String username) {
    User user = getByUsername(username);
    return new PublicUserProfileResponse(user); // excludes email
}

// Restrict updates to skills and bio only
public UserProfileResponse updateProfile(UpdateProfileRequest req, UUID userId) {
    User user = getById(userId);
    if (req.skills() != null) user.setSkills(req.skills());
    if (req.bio() != null) user.setBio(req.bio());
    // ignore username/email fields even if present
    userRepo.save(user);
    return new UserProfileResponse(user);
}

// In UserController
@GetMapping("/my-profile")
public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
    return ResponseEntity.ok(userService.getProfileByUsernameForOwner(principal.getName()));
}

@GetMapping("")
public ResponseEntity<PublicUserProfileResponse> getProfileByUsername(@RequestParam String username) {
    return ResponseEntity.ok(userService.getPublicProfileByUsername(username));
}
```

**Test cases for this phase:**

- `UserServiceTest`:
  - Verify `updateProfile` **does not** change `username` or `email` even if provided in `UpdateProfileRequest`.
  - Verify `getProfileByUsernameForOwner` returns email.
  - Verify `getPublicProfileByUsername` never includes email (DTO has no email field).
- `UserControllerTest` (new or extended):
  - `GET /api/users/my-profile` returns full profile (including email) for authenticated principal.
  - `GET /api/users?username=john` returns public profile without email.

**Technical details and assumptions:**

- Keep `UserProfileResponse` as the owner-facing DTO that includes email.
- Introduce `PublicUserProfileResponse` for public consumption.
- `UpdateProfileRequest` may still contain username/email fields (from previous phases), but they are now ignored by `updateProfile`.
- Security rules in `SecurityConfig` already require auth for `/api/users/my-profile` and allow anonymous access to `GET /api/users?username=`.

### Phase 2: Frontend Profile API Layer (Owner & Public)

**Files**: `dev_connect_frontend/src/app/auth.service.ts` (or `src/app/profile.service.ts` if you choose to separate),
`dev_connect_frontend/src/app/auth-state.service.ts`

**Test Files**: (Optional) `dev_connect_frontend/src/app/auth.service.spec.ts`

Goal: Provide strongly-typed profile APIs for both owner and public views, reusing existing endpoints.

**Key code changes:**

```typescript
// Extend existing interfaces
export interface UserProfile {
  id: string;
  username: string;
  email: string; // owner-only usage
  skills: string[];
  bio?: string;
}

export interface PublicUserProfile {
  id: string;
  username: string;
  skills: string[];
  bio?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = 'http://localhost:8080/api';
  private readonly AUTH_BASE = 'http://localhost:8080/auth';

  constructor(private readonly http: HttpClient) {}

  getMyProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_BASE}/users/my-profile`, {
      withCredentials: true,
    });
  }

  updateMyProfile(update: {
    skills: string[];
    bio?: string;
  }): Observable<UserProfile> {
    return this.http.put<UserProfile>(
      `${this.API_BASE}/users/my-profile`,
      update,
      {
        withCredentials: true,
      },
    );
  }

  getPublicProfile(username: string): Observable<PublicUserProfile> {
    return this.http.get<PublicUserProfile>(`${this.API_BASE}/users`, {
      params: { username },
    });
  }
}
```

**Test cases for this phase:**

- Optional HTTP tests using Angular testing utilities:
  - Ensure `getMyProfile` calls `GET /api/users/my-profile`.
  - Ensure `updateMyProfile` calls `PUT /api/users/my-profile` with `{skills, bio}` payload only.
  - Ensure `getPublicProfile` calls `GET /api/users?username=...`.

**Technical details and assumptions:**

- Continue to use `withCredentials: true` for owner endpoints so the JWT cookie flows.
- Public profile lookup does not require credentials; keep it simple.
- `AuthStateService` can reuse `getMyProfile()` for initial username hydration (already implemented).

### Phase 3: "My Profile" UI (View & Edit)

**Files**: `dev_connect_frontend/src/app/profile/my-profile.ts`,
`dev_connect_frontend/src/app/profile/my-profile.html`,
`dev_connect_frontend/src/app/profile/my-profile.css`,
`dev_connect_frontend/src/app/app.routes.ts`,
`dev_connect_frontend/src/app/navbar/navbar.html`

**Test Files**: (Optional) `dev_connect_frontend/src/app/profile/my-profile.spec.ts`

Goal: Provide a dedicated, authenticated "My profile" screen with editable skills and bio, read-only username/email, and clear success/error feedback.

**Key code changes:**

```typescript
// my-profile.ts (standalone component)
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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      username: [{ value: '', disabled: true }],
      email: [{ value: '', disabled: true }],
      skills: ['', [Validators.required]],
      bio: ['', [Validators.maxLength(500)]],
    });

    this.isLoading = true;
    this.authService.getMyProfile().subscribe({
      next: (profile) => {
        this.isLoading = false;
        this.form.patchValue({
          username: profile.username,
          email: profile.email,
          skills: profile.skills.join(', '),
          bio: profile.bio ?? '',
        });
      },
      error: () => {
        this.isLoading = false;
        this.serverError = 'Unable to load your profile.';
      },
    });
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

    this.isLoading = true;
    this.serverError = null;
    this.successMessage = null;

    this.authService
      .updateMyProfile({ skills, bio: value.bio || undefined })
      .subscribe({
        next: (updated) => {
          this.isLoading = false;
          this.successMessage = 'Profile updated successfully.';
          this.form.patchValue({
            skills: updated.skills.join(', '),
            bio: updated.bio ?? '',
          });
        },
        error: (err) => {
          this.isLoading = false;
          this.serverError =
            err.error?.message || 'Could not update profile. Please try again.';
        },
      });
  }
}
```

```html
<!-- my-profile.html sketch -->
<section class="profile-container" *ngIf="!isLoading; else loadingTpl">
  <h1>My Profile</h1>

  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <div class="readonly-section">
      <div>
        <label>Username</label>
        <input formControlName="username" readonly />
      </div>
      <div class="email-row">
        <label>Email</label>
        <input
          type="password"
          [type]="showEmail ? 'text' : 'password'"
          formControlName="email"
          readonly
        />
        <button type="button" (click)="showEmail = !showEmail">
          {{ showEmail ? 'Hide' : 'Show' }}
        </button>
      </div>
    </div>

    <div class="editable-section">
      <label>Skills (comma-separated)</label>
      <input formControlName="skills" />
      <app-form-field-errors
        [control]="form.get('skills')"
      ></app-form-field-errors>

      <label>Bio</label>
      <textarea formControlName="bio"></textarea>
      <app-form-field-errors
        [control]="form.get('bio')"
      ></app-form-field-errors>
    </div>

    <div class="form-messages">
      <div *ngIf="serverError" class="error">{{ serverError }}</div>
      <div *ngIf="successMessage" class="success">{{ successMessage }}</div>
    </div>

    <button type="submit" [disabled]="isLoading">Save changes</button>
  </form>
</section>

<ng-template #loadingTpl>
  <p>Loading profile...</p>
</ng-template>
```

Routing & navbar wiring:

```typescript
// app.routes.ts
{
  path: 'profile',
  canActivate: [authGuard],
  loadComponent: () => import('./profile/my-profile').then(m => m.MyProfileComponent),
},
```

```html
<!-- navbar.html: ensure Profile link points to /profile and is visible only when authenticated -->
<a routerLink="/profile" class="dropdown-item" (click)="navigateTo('/profile')"
  >My profile</a
>
```

**Test cases for this phase:**

- Form initializes with data from `getMyProfile`.
- Username/email fields are disabled and cannot be edited.
- Skills/bio can be edited and sent back to backend.
- Success and error messages are shown appropriately.
- Email visibility toggle only affects local view (no extra network calls).

**Technical details and assumptions:**

- Reuse `FormFieldErrors` component for consistent validation messages.
- Bio max length (e.g. 500) should align with backend constraints if they exist.
- Skills are managed as a comma-separated string in the UI, mapped to string[] for API.

### Phase 4: Public Profile View & Username Lookup

**Files**: `dev_connect_frontend/src/app/profile/public-profile.ts`,
`dev_connect_frontend/src/app/profile/public-profile.html`,
`dev_connect_frontend/src/app/profile/public-profile.css`,
`dev_connect_frontend/src/app/app.routes.ts`,
`dev_connect_frontend/src/app/navbar/navbar.html`

**Test Files**: (Optional) `dev_connect_frontend/src/app/profile/public-profile.spec.ts`

Goal: Allow any user (signed-in or anonymous) to view another developers public profile by username, without revealing email or editable controls.

**Key code changes:**

```typescript
// public-profile.ts
@Component({
  selector: 'app-public-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './public-profile.html',
  styleUrl: './public-profile.css',
})
export class PublicProfileComponent implements OnInit {
  profile: PublicUserProfile | null = null;
  isLoading = false;
  error: string | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const username = params.get('username');
      if (!username) {
        this.error = 'No username specified.';
        return;
      }
      this.loadProfile(username);
    });
  }

  private loadProfile(username: string): void {
    this.isLoading = true;
    this.error = null;
    this.profile = null;

    this.authService.getPublicProfile(username).subscribe({
      next: (p) => {
        this.isLoading = false;
        this.profile = p;
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 404) {
          this.error = 'No matching developer profile was found.';
        } else {
          this.error = 'Unable to load this profile.';
        }
      },
    });
  }
}
```

```html
<!-- public-profile.html sketch -->
<section class="profile-container" *ngIf="!isLoading; else loadingTpl">
  <ng-container *ngIf="profile; else errorTpl">
    <h1>{{ profile.username }}</h1>
    <p class="subtitle">Public profile</p>

    <div class="public-details">
      <div>
        <h2>Skills</h2>
        <ul>
          <li *ngFor="let skill of profile.skills">{{ skill }}</li>
        </ul>
      </div>
      <div>
        <h2>Bio</h2>
        <p>{{ profile.bio || 'No bio provided yet.' }}</p>
      </div>
    </div>
  </ng-container>
</section>

<ng-template #loadingTpl>
  <p>Loading profile...</p>
</ng-template>

<ng-template #errorTpl>
  <p class="error">{{ error }}</p>
</ng-template>
```

Routing & username lookup:

```typescript
// app.routes.ts
{
  path: 'profiles/:username',
  loadComponent: () => import('./profile/public-profile').then(m => m.PublicProfileComponent),
},
```

```html
<!-- navbar.html: simple username lookup -->
<div class="profile-lookup">
  <input
    type="text"
    placeholder="Find profile by username"
    [(ngModel)]="lookupUsername"
  />
  <button
    (click)="navigateTo('/profiles/' + lookupUsername)"
    [disabled]="!lookupUsername"
  >
    Go
  </button>
</div>
```

**Test cases for this phase:**

- Navigating to `/profiles/john` fetches and displays Johns public profile.
- Email is never displayed (DTO doesnt contain it).
- Non-existent username shows a clear profile not found message.
- Public profile is accessible even when not signed in.

**Technical details and assumptions:**

- Username lookup field should be simple and non-invasive (not a full search system).
- Future work can add links from posts/comments to `/profiles/:username`; this plan only sets up the profile route and lookup.

### Phase 5: Validation, Error Handling & Access Control Polish

**Files**: `dev_connect_frontend/src/app/profile/my-profile.ts`,
`dev_connect_frontend/src/app/profile/my-profile.html`,
`dev_connect_frontend/src/app/profile/public-profile.ts`,
`dev_connect_frontend/src/app/navbar/navbar.html`

**Test Files**: (Shared with previous phases)

Goal: Ensure the UX meets all acceptance criteria around validation, errors, and access rules.

**Key code changes and behaviors:**

```typescript
// my-profile.ts - validation focus
this.form = this.fb.group({
  username: [{ value: '', disabled: true }],
  email: [{ value: '', disabled: true }],
  skills: ['', [Validators.required]],
  bio: ['', [Validators.maxLength(500)]],
});

// onSubmit(): do not clear form on error; keep user input visible
error: (err) => {
  this.isLoading = false;
  this.serverError =
    err.error?.message ||
    'Profile update failed. Please review your changes and try again.';
};
```

Access control expectations:

- **My profile** (`/profile`):
  - Protected by `authGuard`.
  - If not authenticated, guard redirects to `/login` (already implemented in Phase 4 of auth work).
- **Public profile** (`/profiles/:username`):
  - No guard; works for anonymous users.
  - No edit controls rendered; purely read-only view.

**Test cases for this phase:**

- Client-side validation:
  - Empty skills field prevents submit and shows inline error.
  - Over-long bio triggers a clear validation message.
- Server-side failures:
  - When backend returns validation or 5xx errors, error banner is shown and form data remains intact.
- Access control:
  - Hitting `/profile` while logged out sends you to `/login`.
  - Hitting `/profiles/john` while logged out still shows Johns public profile.
  - Logged-in user viewing someone elses profile never sees edit controls.

**Technical details and assumptions:**

- Reuse global HTTP interceptor behavior for auth failures; ensure profile components react appropriately (e.g., do not try to render stale data after 401).
- Align validation messages and styles with existing login/register forms.

## Technical Considerations

- **Dependencies**:
  - No new external libraries required; reuse Spring MVC, Spring Security, Angular HttpClient, and Reactive Forms.
- **Edge Cases**:
  - User has no skills yet: show an empty state message but still require at least one skill on save.
  - User has very long bio from previous system: handle gracefully with truncation or scrolling in the UI.
  - Username lookup with leading/trailing spaces: trim input before navigation.
- **Performance**:
  - Profile endpoints are per-user and low-volume; standard JPA access is sufficient.
  - Avoid repeated network calls by reusing loaded profile data when possible (e.g., if already in `AuthStateService`).
- **Security**:
  - Rely on existing JWT-based auth for `GET/PUT /api/users/my-profile`.
  - Ensure public profile endpoint never includes email or sensitive fields.
  - Confirm CORS continues to allow `http://localhost:4200` with credentials for owner endpoints.

## Testing Notes

- Each phase includes its own tests; do not defer tests to the end.
- Backend:
  - Extend existing `UserServiceTest` to cover new profile rules.
  - Optionally add controller tests for `UserController` profile endpoints.
- Frontend:
  - At minimum, do manual verification of My Profile and Public Profile flows.
  - Where practical, add Angular component tests for form validation and API interaction.
- Run full test suites before merging:
  - Backend: `./gradlew test` from `dev_connect_backend`.
  - Frontend: `npm test` or at least `npm run lint && npm run build` as configured.

## Success Criteria

- [ ] Backend `updateProfile` only mutates `skills` and `bio`, not `username` or `email`.
- [ ] Public profile API never exposes email or other private fields.
- [ ] Signed-in users can view and edit their own skills and bio via the My Profile screen.
- [ ] Username and email appear read-only for the profile owner, with a local-only email show/hide toggle.
- [ ] Any user (including anonymous) can view another developers public profile by username.
- [ ] Non-existent usernames produce a clear "profile not found" message.
- [ ] Access control rules are respected: `/profile` requires login, `/profiles/:username` does not.
- [ ] Validation and error handling match the acceptance criteria for both client-side and server-side failures.
