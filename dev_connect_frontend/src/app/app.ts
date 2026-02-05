import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './navbar/navbar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  isUserAuthenticated: boolean = false;
  loggedInUsername: string | null = null;

  constructor() {}

  onUserLogout():void {
    this.isUserAuthenticated = false;
    this.loggedInUsername = ''
  }
}
