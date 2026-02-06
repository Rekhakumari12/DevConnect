import { Routes } from '@angular/router';
import { Register } from './register/register';
import { Login } from './login/login';
import { Home } from './home/home';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
];
