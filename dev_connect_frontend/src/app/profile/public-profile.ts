import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, PublicUserProfile, Post } from '../auth.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-public-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './public-profile.html',
  styleUrl: './public-profile.css',
})
export class PublicProfileComponent implements OnInit {
  profile: PublicUserProfile | null = null;
  posts: Post[] = [];
  isLoading = false;
  isLoadingPosts = false;
  error: string | null = null;
  currentUsername: string = '';

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
      this.currentUsername = username.trim();
      this.loadProfileAndPosts(this.currentUsername);
    });
  }

  private loadProfileAndPosts(username: string): void {
    this.isLoading = true;
    this.error = null;
    this.profile = null;
    this.posts = [];

    this.authService.getPublicProfile(username).subscribe({
      next: (p) => {
        this.isLoading = false;
        this.profile = p;
        this.loadPosts(username);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 404) {
          this.error = `No matching developer profile was found for "${username}".`;
        } else if (err.status === 0) {
          this.error = 'Unable to connect to server. Please check your connection.';
        } else {
          this.error = 'Unable to load this profile. Please try again.';
        }
      },
    });
  }

  private loadPosts(username: string): void {
    this.isLoadingPosts = true;
    this.authService.getUserPublicPosts(username, 0, 20).subscribe({
      next: (response) => {
        this.isLoadingPosts = false;
        this.posts = response.content;
      },
      error: () => {
        this.isLoadingPosts = false;
        // Silently fail - posts are optional
        this.posts = [];
      },
    });
  }
}
