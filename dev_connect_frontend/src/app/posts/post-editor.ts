import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';
import { FormFieldErrors } from '../common/form-field-errors/form-field-errors';

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
  postId: string | null = null;
  isLoading = false;
  serverError: string | null = null;
  successMessage: string | null = null;

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

    // Check if we're in edit mode
    this.route.paramMap.subscribe((params) => {
      this.postId = params.get('id');
      if (this.postId) {
        this.isEditMode = true;
        this.loadPost(this.postId);
      }
    });
  }

  loadPost(postId: string): void {
    this.isLoading = true;
    this.serverError = null;

    this.authService.getPostById(postId).subscribe({
      next: (post) => {
        this.isLoading = false;
        this.form.patchValue({
          title: post.title,
          content: post.content,
          techStack: post.tags.join(', '),
          visibility: post.visibility,
        });
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 404) {
          this.serverError = 'Post not found';
        } else if (err.status === 403) {
          this.serverError = 'You do not have permission to edit this post';
        } else {
          this.serverError = 'Failed to load post. Please try again.';
        }
      },
    });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.value;
    const techStack = value.techStack
      .split(',')
      .map((t: string) => t.trim())
      .filter((t: string) => !!t);

    if (techStack.length === 0) {
      this.serverError = 'Please provide at least one tech stack item.';
      return;
    }

    const request = {
      title: value.title.trim(),
      content: value.content.trim(),
      techStack: techStack,
      visibility: value.visibility,
    };

    this.isLoading = true;
    this.serverError = null;
    this.successMessage = null;

    const operation = this.isEditMode
      ? this.authService.updatePost(this.postId!, request)
      : this.authService.createPost(request);

    operation.subscribe({
      next: (post) => {
        this.isLoading = false;
        this.successMessage = this.isEditMode
          ? 'Post updated successfully!'
          : 'Post created successfully!';

        // Redirect to browse-ideas after a short delay
        setTimeout(() => {
          this.router.navigate(['/browse-ideas']);
        }, 1500);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 0) {
          this.serverError = 'Unable to connect to server. Please check your connection.';
        } else if (err.status === 401) {
          this.serverError = 'Your session has expired. Please log in again.';
        } else if (err.status === 403) {
          this.serverError = 'You do not have permission to perform this action.';
        } else if (err.status === 400) {
          this.serverError = err.error?.message || 'Invalid post data. Please check your inputs.';
        } else if (err.status === 500) {
          this.serverError = 'Server error. Please try again later.';
        } else {
          this.serverError =
            err.error?.message ||
            `Could not ${this.isEditMode ? 'update' : 'create'} post. Please try again.`;
        }
      },
    });
  }

  onCancel(): void {
    this.router.navigate(['/browse-ideas']);
  }
}
