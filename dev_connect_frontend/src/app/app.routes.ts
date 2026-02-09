import { Routes } from '@angular/router';
import { Register } from './register/register';
import { Login } from './login/login';
import { Home } from './home/home';
import { authGuard } from './auth.guard';
import { guestGuard } from './guest.guard';

export const routes: Routes = [
  { path: 'home', component: Home, canActivate: [authGuard] },
  {
    path: 'profile',
    loadComponent: () => import('./profile/my-profile').then((m) => m.MyProfileComponent),
    canActivate: [authGuard],
  },
  { path: 'login', component: Login },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
];
