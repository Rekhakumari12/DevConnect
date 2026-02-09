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
  username: string;
  password: string;
}

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  skills: string[];
  bio?: string;
}

export interface PublicUserProfile {
  id: string;
  username: string;
  skills: string[];
  bio?: string;
}

export interface UpdateProfileRequest {
  skills: string[];
  bio?: string;
}

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

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Temporary: Use direct backend URL until proxy is fixed
  private readonly API_BASE = 'http://localhost:8080/api';
  private readonly AUTH_BASE = 'http://localhost:8080/auth';

  constructor(private readonly http: HttpClient) {}

  register(payload: RegisterRequest): Observable<any> {
    return this.http.post(`${this.API_BASE}/users/register`, payload, {
      withCredentials: true,
    });
  }

  login(payload: LoginRequest): Observable<any> {
    return this.http.post(`${this.AUTH_BASE}/login`, payload, {
      withCredentials: true,
    });
  }

  logout(): Observable<void> {
    return this.http.post<void>(
      `${this.AUTH_BASE}/logout`,
      {},
      {
        withCredentials: true,
      },
    );
  }

  getMyProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_BASE}/users/my-profile`, {
      withCredentials: true,
    });
  }

  updateMyProfile(update: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.API_BASE}/users/my-profile`, update, {
      withCredentials: true,
    });
  }

  getPublicProfile(username: string): Observable<PublicUserProfile> {
    return this.http.get<PublicUserProfile>(`${this.API_BASE}/users`, {
      params: { username },
    });
  }

  getUserPublicPosts(
    username: string,
    page: number = 0,
    size: number = 10,
  ): Observable<PostsResponse> {
    return this.http.get<PostsResponse>(`${this.API_BASE}/posts`, {
      params: { username, page: page.toString(), size: size.toString() },
    });
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
}
