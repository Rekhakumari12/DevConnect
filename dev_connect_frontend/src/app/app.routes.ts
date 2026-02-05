import { Routes } from '@angular/router';
import { Register } from './register/register';
import { Login } from './login/login';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
];
