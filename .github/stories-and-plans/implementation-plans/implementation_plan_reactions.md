# Reactions Implementation Plan

## Overview

Implement full-stack support for lightweight reactions (LIKE, FUNNY, CELEBRATE, SUPPORT) on posts and comments, with toggle behavior and reaction summaries used for UI badges and future curation.

## Architecture

- **Backend**: Spring Boot endpoints in `ReactionController` calling `ReactionService` with `Reaction` entity and `ReactionType` enum.
- **Frontend**: Angular reaction controls on post and comment UIs, displaying counts and the current user’s selected reaction.
- **Security**: Only authenticated users can react; reactions are toggleable (same type twice removes reaction, different type updates it).

## Implementation Phases

### Phase 1: Backend Reaction Model & Toggle Semantics

**Files**: `dev_connect_backend/src/main/java/com/example/demo/entity/Reaction.java`,
`dev_connect_backend/src/main/java/com/example/demo/enums/ReactionType.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/reaction/ReactionRequest.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/reaction/ReactionResponse.java`,
`dev_connect_backend/src/main/java/com/example/demo/dto/reaction/ReactionSummary.java`,
`dev_connect_backend/src/main/java/com/example/demo/repository/ReactionRepository.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/reaction/ReactionServiceTest.java`

Goal: Confirm data model and toggle rules support one reaction per user per target (post or comment).

**Key code changes:**

```java
// ReactionRequest.java
public record ReactionRequest(@NotNull ReactionType type) {}

// ReactionResponse.java
public record ReactionResponse(
    UUID id,
    ReactionType type,
    UUID userId,
    UUID postId,
    UUID commentId
) {}

// ReactionSummary.java
public record ReactionSummary(
    ReactionType type,
    long count
) {}

// ReactionType.java
public enum ReactionType {
    LIKE, SUPPORT, FUNNY, CELEBRATE
}
```

Test cases for this phase:

- Creating a new reaction inserts a row with correct type and target.
- Reacting again with same type removes existing reaction (toggle off).
- Reacting with different type updates existing record’s type.

Technical details and Assumptions:

- Unique constraint on (userId, postId) and (userId, commentId) enforced at DB level or in service.

### Phase 2: Backend Reaction Service & Controller

**Files**: `dev_connect_backend/src/main/java/com/example/demo/reaction/ReactionService.java`,
`dev_connect_backend/src/main/java/com/example/demo/controller/ReactionController.java`,
`dev_connect_backend/src/main/java/com/example/demo/repository/ReactionRepository.java`,
`dev_connect_backend/src/main/java/com/example/demo/post/PostService.java`,
`dev_connect_backend/src/main/java/com/example/demo/CommentService.java`

**Test Files**: `dev_connect_backend/src/test/java/com/example/demo/reaction/ReactionServiceTest.java`,
`dev_connect_backend/src/test/java/com/example/demo/controller/ReactionControllerTest.java`

Goal: Implement reaction toggling for posts and comments, and endpoints to fetch summaries.

**Key code changes:**

```java
// ReactionService (strategy already exists per context)
@Service
public class ReactionService {

    public ReactionResponse reactToPost(UUID postId, ReactionType type, UUID userId) {
        postService.checkPrivatePost(postId);

        return toggleReaction(
                () -> reactionRepository.findByUserIdAndPostId(userId, postId),
                () -> buildPostReaction(postId, type, userId)
        );
    }

    public ReactionResponse reactToComment(UUID commentId, ReactionType type, UUID userId) {
        Comment comment = commentService.getById(commentId);
        postService.checkPrivatePost(comment.getPost().getId());

        return toggleReaction(
                () -> reactionRepository.findByUserIdAndCommentId(userId, commentId),
                () -> buildCommentReaction(commentId, type, userId)
        );
    }

    private ReactionResponse toggleReaction(
            Supplier<Optional<Reaction>> existingSupplier,
            Supplier<Reaction> newReactionSupplier
    ) {
        Optional<Reaction> existingOpt = existingSupplier.get();
        if (existingOpt.isEmpty()) {
            Reaction saved = reactionRepository.save(newReactionSupplier.get());
            return toResponse(saved);
        }

        Reaction existing = existingOpt.get();
        if (existing.getType() == type) {
            reactionRepository.delete(existing);
            return toResponse(existing); // or null with special status
        }

        existing.setType(type);
        Reaction updated = reactionRepository.save(existing);
        return toResponse(updated);
    }

    public List<ReactionSummary> getReactionsByPostId(UUID postId) {
        return reactionRepository.countByPostIdGrouped(postId);
    }

    public List<ReactionSummary> getReactionsByCommentId(UUID commentId) {
        return reactionRepository.countByCommentIdGrouped(commentId);
    }
}

// ReactionController - already wired
@PostMapping("/posts/{postId}/reactions")
public ResponseEntity<ReactionResponse> reactToPost(
        @PathVariable UUID postId,
        @RequestBody ReactionRequest req,
        @AuthenticationPrincipal UserPrincipal principal
) {
    ReactionResponse reaction = reactionService.reactToPost(postId, req.type(), principal.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(reaction);
}
```

Test cases for this phase:

- POST `/api/posts/{postId}/reactions` toggles correctly per rules.
- POST `/api/comments/{commentId}/reactions` behaves similarly.
- GET `/api/posts/{postId}/reactions` returns counts per type.

Technical details and Assumptions:

- `SecurityConfig` allows GET reaction summary endpoints publicly while protecting POST.

### Phase 3: Frontend Reaction UI for Posts

**Files**: `dev_connect_frontend/src/app/reactions/reaction-bar.ts`,
`dev_connect_frontend/src/app/reactions/reaction-bar.html`,
`dev_connect_frontend/src/app/reactions/reaction-bar.css`,
`dev_connect_frontend/src/app/auth.service.ts`,
`dev_connect_frontend/src/app/browse-ideas/browse-ideas.html`

**Test Files**: `dev_connect_frontend/src/app/reactions/reaction-bar.spec.ts`

Goal: Add a reaction bar under each post card in browse-ideas, showing counts and letting logged-in users react.

**Key code changes:**

```typescript
// auth.service.ts additions
export interface ReactionSummary {
  type: 'LIKE' | 'SUPPORT' | 'FUNNY' | 'CELEBRATE';
  count: number;
}

reactToPost(postId: string, type: string): Observable<any> {
  return this.http.post(`${this.API_BASE}/posts/${postId}/reactions`, { type }, {
    withCredentials: true,
  });
}

getPostReactions(postId: string): Observable<ReactionSummary[]> {
  return this.http.get<ReactionSummary[]>(`${this.API_BASE}/posts/${postId}/reactions`);
}
```

```typescript
// reaction-bar.ts
@Component({
  selector: 'app-reaction-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reaction-bar.html',
  styleUrl: './reaction-bar.css',
})
export class ReactionBarComponent implements OnInit {
  @Input() postId!: string;
  @Input() canReact: boolean = false;

  summaries: ReactionSummary[] = [];
  isLoading = false;

  constructor(private auth: AuthService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.auth.getPostReactions(this.postId).subscribe({
      next: (data) => (this.summaries = data),
      error: () => {},
    });
  }

  onReact(type: ReactionSummary['type']): void {
    if (!this.canReact) return;
    this.auth.reactToPost(this.postId, type).subscribe({
      next: () => this.load(),
      error: () => {},
    });
  }
}
```

```html
<!-- reaction-bar.html -->
<div class="reaction-bar">
  <button
    *ngFor="let r of ['LIKE','SUPPORT','FUNNY','CELEBRATE']"
    type="button"
    (click)="onReact(r as any)"
    [disabled]="!canReact"
  >
    {{ r }}
    <span class="count"> {{ (summaries | reactionCount:r) || 0 }} </span>
  </button>
</div>
```

Test cases for this phase:

- Reaction counts update after reacting.
- Buttons disabled for unauthenticated users.
- Toggling the same reaction decreases the count appropriately.

Technical details and Assumptions:

- Optional `reactionCount` pipe can be implemented to map summaries to counts.

### Phase 4: Frontend Reactions on Comments & UX Polish

**Files**: `dev_connect_frontend/src/app/reactions/comment-reaction-bar.ts`,
`dev_connect_frontend/src/app/reactions/comment-reaction-bar.html`,
`dev_connect_frontend/src/app/comments/comments-list.html`

**Test Files**: `dev_connect_frontend/src/app/reactions/comment-reaction-bar.spec.ts`

Goal: Allow the same reaction mechanism on comments, integrated into the comments list.

**Key code changes:**

```typescript
// auth.service.ts additions
reactToComment(commentId: string, type: string): Observable<any> {
  return this.http.post(`${this.API_BASE}/comments/${commentId}/reactions`, { type }, {
    withCredentials: true,
  });
}

getCommentReactions(commentId: string): Observable<ReactionSummary[]> {
  return this.http.get<ReactionSummary[]>(`${this.API_BASE}/comments/${commentId}/reactions`);
}
```

```html
<!-- comments-list.html -->
<li *ngFor="let c of comments">
  <!-- existing meta + content -->
  <app-comment-reaction-bar
    [commentId]="c.id"
    [canReact]="canComment"
  ></app-comment-reaction-bar>
</li>
```

Test cases for this phase:

- Reactions on comments behave like reactions on posts.
- Counts appear correctly under each comment.

Technical details and Assumptions:

- For MVP, UI does not show which specific reaction the current user selected; only counts.

## Technical Considerations

- **Dependencies**: Reuse existing ReactionService and ReactionController.
- **Edge Cases**:
  - Reactions must be disabled on PRIVATE posts and their comments.
  - Deleting a post/comment should cascade or clean up reactions.
- **Testing Strategy**:
  - Backend: ReactionServiceTest and controller tests.
  - Frontend: manual testing plus small unit tests for reaction components.
- **Performance**:
  - Reaction summary queries should use aggregation at DB level.

## Testing Notes

- Backend: run `./gradlew test` and ensure new reaction tests pass.
- Frontend: manually verify reaction behaviors on posts and comments.

## Success Criteria

- [ ] Users can react to posts and comments with LIKE/SUPPORT/FUNNY/CELEBRATE.
- [ ] Toggling same reaction removes it; changing type updates it.
- [ ] Reaction summaries are visible on posts and comments.
- [ ] Reactions are disallowed on PRIVATE posts and their comments.
- [ ] Only authenticated users can react; anonymous users see counts only.
