import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, Post, UserProfile } from '../auth.service';
import { AuthStateService } from '../auth-state.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class DashboardComponent implements OnInit {
  profile: UserProfile | null = null;
  myPosts: Post[] = [];
  isLoading = false;
  isLoadingPosts = false;
  error: string | null = null;

  publicPosts: Post[] = [];
  privatePosts: Post[] = [];

  constructor(
    private readonly authService: AuthService,
    private readonly authStateService: AuthStateService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadMyPosts();
  }

  private loadProfile(): void {
    this.isLoading = true;
    this.error = null;

    this.authService.getMyProfile().subscribe({
      next: (profile) => {
        this.isLoading = false;
        this.profile = profile;
      },
      error: (err) => {
        this.isLoading = false;
        this.error = 'Failed to load profile';
      },
    });
  }

  private loadMyPosts(): void {
    this.isLoadingPosts = true;

    this.authService.getMyPosts().subscribe({
      next: (posts) => {
        this.isLoadingPosts = false;
        this.myPosts = posts;
        this.publicPosts = posts.filter((p) => p.visibility === 'PUBLIC');
        this.privatePosts = posts.filter((p) => p.visibility === 'PRIVATE');
      },
      error: (err) => {
        this.isLoadingPosts = false;
        console.error('Failed to load posts:', err);
      },
    });
  }

  onViewPost(postId: string): void {
    this.router.navigate(['/posts', postId]);
  }

  onEditPost(postId: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/posts', postId, 'edit']);
  }

  onDeletePost(postId: string, postTitle: string, event: Event): void {
    event.stopPropagation();

    if (!confirm(`Are you sure you want to delete "${postTitle}"?`)) {
      return;
    }

    this.authService.deletePost(postId).subscribe({
      next: () => {
        this.myPosts = this.myPosts.filter((p) => p.id !== postId);
        this.publicPosts = this.publicPosts.filter((p) => p.id !== postId);
        this.privatePosts = this.privatePosts.filter((p) => p.id !== postId);
        alert('Post deleted successfully!');
      },
      error: (err) => {
        console.error('Error deleting post:', err);
        alert('Failed to delete post. Please try again.');
      },
    });
  }

  onPublishPost(postId: string, event: Event): void {
    event.stopPropagation();

    const post = this.myPosts.find((p) => p.id === postId);
    if (!post) return;

    const request = {
      title: post.title,
      content: post.content,
      techStack: post.tags,
      visibility: 'PUBLIC',
    };

    this.authService.updatePost(postId, request).subscribe({
      next: () => {
        this.loadMyPosts();
      },
      error: (err) => {
        console.error('Error publishing post:', err);
        alert('Failed to publish post. Please try again.');
      },
    });
  }
}
