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

  getMyProfile(): Observable<any> {
    return this.http.get(`${this.API_BASE}/users/my-profile`, {
      withCredentials: true,
    });
  }
}
