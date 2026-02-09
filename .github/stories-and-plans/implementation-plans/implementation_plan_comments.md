# Comments Implementation Plan

## Overview

Implement full-stack support for comments on public posts: authenticated users can add comments to PUBLIC posts, view comments on a post, and delete only their own comments.

## Architecture

- **Backend**: Spring Boot REST endpoints in `CommentController` backed by `CommentService`, `CommentRepository`, and `Comment` entity.
- **Frontend**: Angular comment UI embedded on the post detail or browse page, calling comment APIs via a service.
- **Security**: Only authenticated users can create/delete comments; comments allowed only on PUBLIC posts.

## Implementation Phases

### Phase 1: Backend Comment Model & DTOs

**Files**: `dev_connect_backend/src/main/java/com/example/demo/entity/Comment.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/comment/CommentRequest.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/comment/CommentResponse.java`,
`dev_connect_backend/src/main/java/com/example/demo/repository/CommentRepository.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/repository/CommentRepositoryTest.java`

Goal: Ensure the domain model and DTOs reflect business rules (each comment linked to user and post).

**Key code changes:**

```java
// CommentRequest.java
public record CommentRequest(
    @NotBlank String content
) {}

// CommentResponse.java
public record CommentResponse(
    UUID id,
    String content,
    String username,
    Instant createdAt
) {}

// Comment entity (if not already present)
@Entity
public class Comment {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Post post;

    @Column(nullable = false, length = 1000)
    private String content;

    private Instant createdAt;
}
```

Test cases for this phase:

- CommentRepository can save and fetch comments by postId.
- `content` cannot be null/blank.

Technical details and Assumptions:

- `createdAt` can be handled via JPA auditing or set manually on save.

### Phase 2: Backend Comment Service & Controller Behavior

**Files**: `dev_connect_backend/src/main/java/com/example/demo/CommentService.java`,
`dev_connect_backend/src/main/java/com/example/demo/controller/CommentController.java`,
`dev_connect_backend/src/main/java/com/example/demo/post/PostService.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/CommentServiceTest.java`,
`dev_connect_backend/src/test/java/com/example/demo/controller/CommentControllerTest.java`

Goal: Implement add, list, and delete comment operations, enforcing ownership and post visibility.

**Key code changes:**

```java
// CommentService (existing)
@Service
public class CommentService {

    public CommentResponse addComment(UUID postId, String content, String username) {
        Post post = postService.getById(postId);
        postService.checkPrivatePost(postId); // disallow comments on PRIVATE posts

        User user = userService.getByUsername(username);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(Instant.now());

        Comment saved = commentRepository.save(comment);
        return toResponse(saved);
    }

    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Ownership enforcement via AuthUtil
        AuthUtil.verifyUserAccess(comment.getUser().getUsername());
        commentRepository.delete(comment);
    }

    public List<CommentResponse> getCommentByPostId(UUID postId) {
        postService.checkPrivatePost(postId);
        return commentRepository.findByPostId(postId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                comment.getCreatedAt()
        );
    }
}

// CommentController (already present)
@PostMapping("/posts/{postId}/comments")
public ResponseEntity<CommentResponse> addComment(
        @PathVariable UUID postId,
        @Valid @RequestBody CommentRequest req,
        Principal principal
) {
    CommentResponse response = commentService.addComment(postId, req.content(), principal.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

Test cases for this phase:

- Adding a comment to PUBLIC post returns 201 and correct username/content.
- Adding a comment to PRIVATE post returns 403.
- Deleting own comment succeeds; deleting someone else’s comment returns 403.
- Listing comments for PUBLIC post returns ordered list (e.g., by createdAt).

Technical details and Assumptions:

- `AuthUtil.verifyUserAccess` ensures current principal matches comment owner.

### Phase 3: Frontend Comment UI – List & Add Comments

**Files**: `dev_connect_frontend/src/app/comments/comments-list.ts`,
`dev_connect_frontend/src/app/comments/comments-list.html`,
`dev_connect_frontend/src/app/comments/comments-list.css`,
`dev_connect_frontend/src/app/auth.service.ts`,
`dev_connect_frontend/src/app/browse-ideas/browse-ideas.html`

**Test Files**: `dev_connect_frontend/src/app/comments/comments-list.spec.ts`

Goal: Provide an embedded comment section under each post, allowing authenticated users to add comments and all users to view them.

**Key code changes:**

```typescript
// auth.service.ts additions
export interface Comment {
  id: string;
  content: string;
  username: string;
  createdAt: string;
}

addComment(postId: string, content: string): Observable<Comment> {
  return this.http.post<Comment>(`${this.API_BASE}/posts/${postId}/comments`, { content }, {
    withCredentials: true,
  });
}

getComments(postId: string): Observable<Comment[]> {
  return this.http.get<Comment[]>(`${this.API_BASE}/posts/${postId}/comments`);
}

deleteComment(commentId: string): Observable<void> {
  return this.http.delete<void>(`${this.API_BASE}/comments/${commentId}`, {
    withCredentials: true,
  });
}
```

```typescript
// comments-list.ts - standalone reusable component
@Component({
  selector: 'app-comments-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './comments-list.html',
  styleUrl: './comments-list.css',
})
export class CommentsListComponent implements OnInit {
  @Input() postId!: string;
  @Input() canComment: boolean = false;

  comments: Comment[] = [];
  form!: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      content: ['', [Validators.required, Validators.maxLength(1000)]],
    });
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.auth.getComments(this.postId).subscribe({
      next: (comments) => {
        this.comments = comments;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load comments';
        this.isLoading = false;
      },
    });
  }

  onSubmit(): void {
    if (!this.canComment || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const content = this.form.value.content.trim();
    if (!content) return;

    this.auth.addComment(this.postId, content).subscribe({
      next: (comment) => {
        this.comments = [...this.comments, comment];
        this.form.reset();
      },
      error: () => {
        this.errorMessage = 'Failed to add comment';
      },
    });
  }
}
```

Test cases for this phase:

- Comments list loads when post is visible (PUBLIC).
- Authenticated user can add a comment; list updates immediately.
- Unauthenticated user sees comments but cannot add (no input shown).

Technical details and Assumptions:

- `canComment` input is driven by parent component based on auth state and post visibility.

### Phase 4: Frontend Comment Deletion & UX Polish

**Files**: `dev_connect_frontend/src/app/comments/comments-list.html`,
`dev_connect_frontend/src/app/comments/comments-list.css`

**Test Files**: `dev_connect_frontend/src/app/comments/comments-list.spec.ts`

Goal: Allow users to delete their own comments and polish UX (timestamps, empty state, errors).

**Key code changes:**

```html
<!-- comments-list.html -->
<div class="comments-section">
  <h3>Comments</h3>

  <form *ngIf="canComment" [formGroup]="form" (ngSubmit)="onSubmit()">
    <textarea
      formControlName="content"
      placeholder="Add a comment..."
    ></textarea>
    <button type="submit" [disabled]="form.invalid">Post Comment</button>
  </form>

  <div *ngIf="comments.length === 0" class="empty-state">
    <p>No comments yet. Be the first to comment.</p>
  </div>

  <ul *ngIf="comments.length > 0">
    <li *ngFor="let c of comments">
      <div class="meta">
        <span class="author">{{ c.username }}</span>
        <span class="date">{{ c.createdAt | date: 'short' }}</span>
        <button
          *ngIf="canComment && c.username === currentUsername"
          type="button"
          (click)="onDelete(c.id)"
        >
          Delete
        </button>
      </div>
      <p>{{ c.content }}</p>
    </li>
  </ul>
</div>
```

Test cases for this phase:

- Delete button visible only on own comments.
- On delete, comment disappears from list without full reload.
- Errors during delete show a non-blocking message.

Technical details and Assumptions:

- `currentUsername` can be passed as `@Input` from parent using `AuthStateService`.

## Technical Considerations

- **Dependencies**: Reuse existing CommentService and PostService.checkPrivatePost.
- **Edge Cases**:
  - Comments on PRIVATE posts must be rejected.
  - Rapid repeated submissions should be prevented by disabling the button while request in flight.
- **Testing Strategy**:
  - Backend: unit tests for CommentService, controller tests for CommentController.
  - Frontend: component tests for CommentsList where feasible; at minimum, manual testing.
- **Performance**:
  - For now, simple list load per post; pagination not required for MVP.

## Testing Notes

- Backend: run `./gradlew test` and ensure new comment tests pass.
- Frontend: manually verify comment behaviors for both authenticated and anonymous users.

## Success Criteria

- [ ] Authenticated users can add comments to PUBLIC posts.
- [ ] Comments on PRIVATE posts are rejected with 403.
- [ ] Users can delete only their own comments.
- [ ] Comments are visible on public posts for all users.
- [ ] Frontend comment UI behaves correctly with clear messages.
