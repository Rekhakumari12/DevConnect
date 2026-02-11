import { Routes } from '@angular/router';
import { Register } from './register/register';
import { Login } from './login/login';
import { authGuard } from './auth.guard';
import { guestGuard } from './guest.guard';

export const routes: Routes = [
  {
    path: 'home',
    loadComponent: () => import('./dashboard/dashboard').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./dashboard/dashboard').then((m) => m.DashboardComponent),
    canActivate: [authGuard],
  },
  {
    path: 'browse-ideas',
    loadComponent: () => import('./browse-ideas/browse-ideas').then((m) => m.BrowseIdeasComponent),
    canActivate: [authGuard],
  },
  {
    path: 'posts/new',
    loadComponent: () => import('./posts/post-editor').then((m) => m.PostEditorComponent),
    canActivate: [authGuard],
  },
  {
    path: 'posts/:id',
    loadComponent: () => import('./posts/post-detail').then((m) => m.PostDetailComponent),
    canActivate: [authGuard],
  },
  {
    path: 'posts/:id/edit',
    loadComponent: () => import('./posts/post-editor').then((m) => m.PostEditorComponent),
    canActivate: [authGuard],
  },
  {
    path: 'profile',
    loadComponent: () => import('./profile/my-profile').then((m) => m.MyProfileComponent),
    canActivate: [authGuard],
  },
  {
    path: 'profile/:username',
    loadComponent: () => import('./profile/public-profile').then((m) => m.PublicProfileComponent),
  },
  { path: 'login', component: Login },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
];
