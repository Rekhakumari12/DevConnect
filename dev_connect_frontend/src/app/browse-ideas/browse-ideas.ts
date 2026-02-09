import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService, PublicUserProfile } from '../auth.service';
import { AuthStateService } from '../auth-state.service';

@Component({
  selector: 'app-browse-ideas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './browse-ideas.html',
  styleUrls: ['./browse-ideas.css'],
})
export class BrowseIdeasComponent implements OnInit {
  searchQuery: string = '';
  techStack: string = '';
  posts: any[] = [];
  users: PublicUserProfile[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  currentUsername: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private authStateService: AuthStateService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Get current username for ownership checks
    this.authStateService.currentUser$.subscribe((user) => {
      this.currentUsername = user?.username || null;
    });

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

  searchPosts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.posts = [];
    this.users = [];

    // If searching by general query, search for both users and posts
    if (this.searchQuery) {
      // Search for users by username
      this.authService.getPublicProfile(this.searchQuery).subscribe({
        next: (user) => {
          this.users = [user];
        },
        error: () => {
          // User not found, that's okay
          this.users = [];
        },
      });

      // Search for posts by keyword
      this.authService.searchPosts(this.searchQuery).subscribe({
        next: (response) => {
          this.posts = response.content;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error searching posts:', err);
          this.errorMessage = 'Failed to search posts';
          this.isLoading = false;
        },
      });
    } else if (this.techStack) {
      // Search posts by tech stack (using search endpoint with tech stack as keyword)
      this.authService.searchPosts(this.techStack).subscribe({
        next: (response) => {
          this.posts = response.content;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error filtering by tech stack:', err);
          this.errorMessage = 'Failed to filter posts';
          this.isLoading = false;
        },
      });
    }
  }

  loadAllPosts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.getPublicPosts().subscribe({
      next: (response) => {
        this.posts = response.content;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading posts:', err);
        this.errorMessage = 'Failed to load posts';
        this.isLoading = false;
      },
    });
  }

  isPostOwner(post: any): boolean {
    return this.currentUsername !== null && post.username === this.currentUsername;
  }

  onEditPost(postId: string): void {
    this.router.navigate(['/posts', postId, 'edit']);
  }

  onDeletePost(postId: string, postTitle: string): void {
    if (!confirm(`Are you sure you want to delete "${postTitle}"?`)) {
      return;
    }

    this.authService.deletePost(postId).subscribe({
      next: () => {
        this.posts = this.posts.filter((p) => p.id !== postId);
      },
      error: (err) => {
        console.error('Error deleting post:', err);
        alert('Failed to delete post. Please try again.');
      },
    });
  }
}
