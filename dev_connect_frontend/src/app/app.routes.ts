import { Routes } from '@angular/router';
import { Register } from './register/register';
import { Login } from './login/login';
import { Home } from './home/home';
import { authGuard } from './auth.guard';
import { guestGuard } from './guest.guard';

export const routes: Routes = [
  { path: 'home', component: Home, canActivate: [authGuard] },
  {
    path: 'browse-ideas',
    loadComponent: () => import('./browse-ideas/browse-ideas').then((m) => m.BrowseIdeasComponent),
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
