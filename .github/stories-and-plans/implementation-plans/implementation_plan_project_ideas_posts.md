# Project Ideas (Posts) Implementation Plan

## Overview

Implement full-stack support for project idea posts, allowing authenticated users to create, view, edit, delete, and browse public project ideas with title, description, tech stack, and visibility (PUBLIC/PRIVATE), reusing the existing Post entity/service.

## Architecture

- **Backend**: Spring Boot REST endpoints in `PostController` backed by `PostService`, `PostRepository`, and `Post` entity. Use DTOs `PostRequest`/`PostResponse` and visibility enum `PostVisibility`.
- **Frontend**: Angular routes for browsing ideas and (later) creating/editing posts. Use `AuthService` for HTTP, `BrowseIdeasComponent` for listing/search.
- **Security**: JWT-based auth with `UserPrincipal`; ownership enforced by `PostService.getOwnedPost` and `AccessDeniedException`.

## Implementation Phases

### Phase 1: Backend Post Creation & Ownership Rules

**Files**: `dev_connect_backend/src/main/java/com/example/demo/post/PostService.java`,
`dev_connect_backend/src/main/java/com/example/demo/controller/PostController.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/post/PostRequest.java`,
`dev_connect_backend/src/main/java/com/example/demo/entity/Post.java`,
`dev_connect_backend/src/main/java/com/example/demo/enums/PostVisibility.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/post/PostServiceTest.java` (create),
`dev_connect_backend/src/test/java/com/example/demo/controller/PostControllerTest.java`

Goal: Ensure posts can be created with correct ownership, fields, and visibility.

**Key code changes:**

```java
// PostRequest.java
public record PostRequest(
    @NotBlank String title,
    @NotBlank String content,
    @NotNull List<String> techStack,
    @NotNull PostVisibility visibility
) {}

// PostService.createPost (already exists – verify behavior)
public PostResponse createPost(PostRequest postRequest, UUID userId) {
    User user = userService.getById(userId);
    Post post = new Post();
    post.setUser(user);
    post.setTitle(postRequest.title());
    post.setContent(postRequest.content());
    post.setTags(postRequest.techStack());
    post.setVisibility(postRequest.visibility());
    Post saved = postRepo.save(post);
    return toResponse(saved);
}

// PostController
@PostMapping("")
public ResponseEntity<PostResponse> createPost(
        @Valid @RequestBody PostRequest post,
        @AuthenticationPrincipal UserPrincipal p
) {
    PostResponse postResponse = postService.createPost(post, p.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
}
```

Test cases for this phase:

- Creating a post with valid title/content/techStack/visibility returns 201 and owner set to current user.
- Creating with missing required fields returns 400 with validation error payload.
- Visibility must be either PUBLIC or PRIVATE; invalid value rejected.

Technical details and Assumptions:

- Reuse existing `Post` entity fields (`title`, `content`, `tags`, `visibility`, timestamps).
- Assume JPA auditing or manual setting of `createdAt`/`updatedAt` on `Post`.
- Ownership stored via `Post.user` (many-to-one to `User`).

### Phase 2: Backend Post Retrieval, Visibility & Pagination

**Files**: `dev_connect_backend/src/main/java/com/example/demo/post/PostService.java`,
`dev_connect_backend/src/main/java/com/example/demo/controller/PostController.java`,
`dev_connect_backend/src/main/java/com/example/demo/repository/PostRepository.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/post/PostServiceTest.java`,
`dev_connect_backend/src/test/java/com/example/demo/controller/PostControllerTest.java`

Goal: Support listing public posts, current user posts, and posts by username, respecting visibility rules and pagination.

**Key code changes:**

```java
// PostService - already present but validate behavior
public Page<PostResponse> getPublicPosts(Integer page, Integer size) {
    Pageable pageable = PageRequest.of(
            page == null ? 0 : page,
            size == null ? 10 : size
    );

    Page<Post> posts = postRepo.findByVisibility(PostVisibility.PUBLIC, pageable);
    return posts.map(this::toResponse);
}

public List<PostResponse> getPostsByUsername(String username) {
    PostFetchStrategy strategy =
            AuthUtil.isAuthenticated(username)
                    ? new LoggedInPostFetchStrategy(postRepo)
                    : new PublicPostFetchStrategy(postRepo);

    return strategy.fetchPosts(username)
            .stream()
            .map(this::toResponse)
            .toList();
}

// PostController - already present but align with PRD
@GetMapping("/public")
public ResponseEntity<Page<PostResponse>> getAllPost(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Page<PostResponse> posts = postService.getPublicPosts(page, size);
    return ResponseEntity.ok(posts);
}

@GetMapping("")
public ResponseEntity<List<PostResponse>> getPostsByUsername(@RequestParam String username) {
    List<PostResponse> posts = postService.getPostsByUsername(username);
    return ResponseEntity.ok(posts);
}

@GetMapping("/my-post")
public ResponseEntity<List<PostResponse>> getMyPosts(@AuthenticationPrincipal UserPrincipal p) {
    List<PostResponse> posts = postService.getPostsByUsername(p.getUsername());
    return ResponseEntity.ok(posts);
}
```

Test cases for this phase:

- `/api/posts/public` returns only PUBLIC posts, paginated (default size 10).
- `/api/posts?username=john` returns John’s PUBLIC posts when anonymous.
- `/api/posts/my-post` returns both PUBLIC and PRIVATE posts for the logged-in owner.
- PRIVATE posts never appear in public endpoints.

Technical details and Assumptions:

- `PostFetchStrategy` hierarchy already selects appropriate queries for logged-in vs public.
- `SecurityConfig` already permits `GET /api/posts/public` to everyone.

### Phase 3: Backend Post Update & Delete with Ownership Enforcement

**Files**: `dev_connect_backend/src/main/java/com/example/demo/post/PostService.java`,
`dev_connect_backend/src/main/java/com/example/demo/controller/PostController.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/post/PostServiceTest.java`

Goal: Ensure only the owner can update or delete a post; enforce field-level rules.

**Key code changes:**

```java
// PostService.updatePost - already present; verify rules
public PostResponse updatePost(PostRequest req, String username, UUID postId) {
    Post post = getOwnedPost(username, postId);
    if (req.title() != null) post.setTitle(req.title());
    if (req.content() != null) post.setContent(req.content());
    if (req.techStack() != null) post.setTags(req.techStack());
    if (req.visibility() != null) post.setVisibility(req.visibility());
    Post updatedPost = postRepo.save(post);
    return toResponse(updatedPost);
}

// Ownership helper
private Post getOwnedPost(String username, UUID postId) {
    return postRepo.findByIdAndUser_Username(postId, username)
            .orElseThrow(() -> new AccessDeniedException("Access Denied"));
}

// PostController endpoints already wired
@PutMapping("/{postId}")
public ResponseEntity<PostResponse> updatePost(
        @RequestBody PostRequest req,
        @PathVariable UUID postId,
        @AuthenticationPrincipal UserPrincipal p
) {
    PostResponse post = postService.updatePost(req, p.getUsername(), postId);
    return ResponseEntity.ok(post);
}

@DeleteMapping("/{postId}")
public ResponseEntity<Void> deletePost(
        @PathVariable UUID postId,
        @AuthenticationPrincipal UserPrincipal p
) {
    postService.deletePost(postId, p.getUsername());
    return ResponseEntity.noContent().build();
}
```

Test cases for this phase:

- Owner can update title/content/techStack/visibility of their post.
- Non-owner receives 403 Access Denied when trying to update/delete.
- Deleting a post removes it and associated comments/reactions as per JPA cascade rules (or explicit deletes).

Technical details and Assumptions:

- Deletion of comments/reactions may be handled via DB cascading or repository deletes; plan tests accordingly.

### Phase 4: Frontend – Browse Public Posts & Search/Filter

**Files**: `dev_connect_frontend/src/app/auth.service.ts`,
`dev_connect_frontend/src/app/browse-ideas/browse-ideas.ts`,
`dev_connect_frontend/src/app/browse-ideas/browse-ideas.html`,
`dev_connect_frontend/src/app/browse-ideas/browse-ideas.css`,
`dev_connect_frontend/src/app/app.routes.ts`,
`dev_connect_frontend/src/app/navbar/navbar.ts`,
`dev_connect_frontend/src/app/navbar/navbar.html`

**Test Files**: (optional) `dev_connect_frontend/src/app/browse-ideas/browse-ideas.spec.ts`

Goal: Provide a UI for listing project ideas, searching by keyword, and filtering by tech stack, wired to backend APIs.

**Key code changes:**

```typescript
// auth.service.ts - already partially implemented
export interface Post {
  id: string;
  title: string;
  content: string;
  tags: string[];
  visibility: string;
  createdAt: string;
  username?: string;
}

export interface PostsResponse {
  content: Post[];
  totalElements: number;
  totalPages: number;
  number: number;
}

getPublicPosts(page: number = 0, size: number = 20): Observable<PostsResponse> {
  return this.http.get<PostsResponse>(`${this.API_BASE}/posts/public`, {
    params: { page: page.toString(), size: size.toString() },
  });
}

searchPosts(keyword: string, page: number = 0, size: number = 20): Observable<PostsResponse> {
  return this.http.get<PostsResponse>(`${this.API_BASE}/search`, {
    params: { keyword, page: page.toString(), size: size.toString() },
  });
}
```

```typescript
// browse-ideas.ts - already implemented at a basic level
ngOnInit(): void {
  this.route.queryParams.subscribe((params) => {
    this.searchQuery = params['q'] || '';
    this.techStack = params['tech'] || '';

    if (this.searchQuery || this.techStack) {
      this.searchPosts();
    } else {
      this.loadAllPosts();
    }
  });
}
```

Test cases for this phase:

- `/browse-ideas` loads public posts from backend.
- Searching by keyword uses `/api/search?keyword=` and updates the list.
- Selecting a tech stack uses the same search endpoint with the stack as keyword.
- Empty results show a clear “no posts found” message.

Technical details and Assumptions:

- `authGuard` already protects `/browse-ideas` for authenticated users.
- Navbar search and tech stack filter already navigate with `q` and `tech` query params.

### Phase 5: Frontend – Create & Edit Post UI

**Files**: `dev_connect_frontend/src/app/posts/post-editor.ts`,
`dev_connect_frontend/src/app/posts/post-editor.html`,
`dev_connect_frontend/src/app/posts/post-editor.css`,
`dev_connect_frontend/src/app/app.routes.ts`,
`dev_connect_frontend/src/app/auth.service.ts`

**Test Files**: `dev_connect_frontend/src/app/posts/post-editor.spec.ts`

Goal: Provide a form for creating and editing posts (title, description/content, tech stack, visibility) for authenticated users.

**Key code changes:**

```typescript
// post-editor.ts - standalone component
@Component({
  selector: 'app-post-editor',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FormFieldErrors],
  templateUrl: './post-editor.html',
  styleUrl: './post-editor.css',
})
export class PostEditorComponent implements OnInit {
  form!: FormGroup;
  isEditMode = false;
  isLoading = false;
  serverError: string | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(120)]],
      content: ['', [Validators.required, Validators.maxLength(5000)]],
      techStack: ['', [Validators.required]],
      visibility: ['PUBLIC', [Validators.required]],
    });

    // If route has postId param, switch to edit mode and load post
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.value;
    const request: PostRequest = {
      title: value.title,
      content: value.content,
      techStack: value.techStack
        .split(',')
        .map((t: string) => t.trim())
        .filter(Boolean),
      visibility: value.visibility,
    };

    // Call create or update endpoint via AuthService/PostService wrapper
  }
}
```

Test cases for this phase:

- Creating a new post with valid data redirects to browse-ideas or post list.
- Editing an existing post pre-fills the form and updates correctly.
- Validation prevents empty title/content/techStack and shows inline errors.

Technical details and Assumptions:

- Might introduce a dedicated `PostApiService` if `AuthService` becomes too large.
- For MVP, post editor is accessible via `/posts/new` and `/posts/:id/edit` routes, protected by `authGuard`.

## Technical Considerations

- **Dependencies**: Reuse existing Spring Data JPA, PostRepository, PostService, and Angular HttpClient.
- **Edge Cases**:
  - Private posts must never appear in public lists or search.
  - Deleting a post with comments/reactions must not violate referential integrity.
  - Large content fields should be truncated in list views.
- **Testing Strategy**:
  - Backend unit tests for PostService and integration tests for PostController.
  - Frontend component tests for browse-ideas and post-editor where feasible.
- **Performance**:
  - Use pagination for lists; avoid loading all posts at once.
  - Search endpoint should be indexed on title and tags.
- **Security**:
  - Enforce ownership checks on update/delete.
  - Ensure only authenticated users can create or edit posts.

## Testing Notes

- Backend: run `./gradlew test` in `dev_connect_backend` and ensure new tests pass.
- Frontend: eventually add component tests; for now, manual verification via `npm start` is acceptable.
- Verify error handling and validation paths in both layers.

## Success Criteria

- [ ] Authenticated users can create posts with title, content, tech stack, and visibility.
- [ ] Public posts are visible to everyone via `/api/posts/public` and `/browse-ideas`.
- [ ] Only the owner can update or delete their posts; others receive 403.
- [ ] Private posts are only visible to the owner and excluded from public lists/search.
- [ ] Frontend browse/search UI correctly reflects backend data and validation.
- [ ] Post editor enforces validation and surfaces backend errors clearly.
