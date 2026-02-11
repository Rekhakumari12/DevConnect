import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService, Post } from '../auth.service';
import { AuthStateService } from '../auth-state.service';
import { NewlineToParagraphPipe } from '../common/newline-to-paragraph.pipe';

@Component({
  selector: 'app-post-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, NewlineToParagraphPipe],
  templateUrl: './post-detail.html',
  styleUrl: './post-detail.css',
})
export class PostDetailComponent implements OnInit {
  post: Post | null = null;
  isLoading = false;
  error: string | null = null;
  currentUsername: string | null = null;
  isOwner = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly authStateService: AuthStateService,
  ) {}

  ngOnInit(): void {
    // Get current user
    this.authStateService.currentUser$.subscribe((user) => {
      this.currentUsername = user?.username || null;
    });

    // Load post
    this.route.paramMap.subscribe((params) => {
      const postId = params.get('id');
      if (postId) {
        this.loadPost(postId);
      } else {
        this.error = 'No post ID provided';
      }
    });
  }

  private loadPost(postId: string): void {
    this.isLoading = true;
    this.error = null;

    this.authService.getPostById(postId).subscribe({
      next: (post) => {
        this.isLoading = false;
        this.post = post;
        this.isOwner = this.currentUsername !== null && post.username === this.currentUsername;
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 404) {
          this.error = 'Post not found';
        } else if (err.status === 403) {
          this.error = 'You do not have permission to view this post';
        } else {
          this.error = 'Failed to load post';
        }
      },
    });
  }

  onEditPost(): void {
    if (this.post) {
      this.router.navigate(['/posts', this.post.id, 'edit']);
    }
  }

  onDeletePost(): void {
    if (!this.post) return;

    if (!confirm(`Are you sure you want to delete "${this.post.title}"?`)) {
      return;
    }

    this.authService.deletePost(this.post.id).subscribe({
      next: () => {
        alert('Post deleted successfully!');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Error deleting post:', err);
        alert('Failed to delete post. Please try again.');
      },
    });
  }

  onBack(): void {
    this.router.navigate(['/browse-ideas']);
  }
}
