import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  standalone: true,
})
export class Navbar {
  @Input() isAuthenticated: boolean = false;
  @Input() username: string | null = null;

  @Output() logout = new EventEmitter<void>();

  isDropdownOpen: boolean = false;

  // The empty constructor is now essential for DI
  constructor(private router: Router) {
    // Angular reads 'private router: Router' and injects the service
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  onLogout(): void {
    this.isDropdownOpen = false;
    this.logout.emit();
  }

  navigateTo(path: string): void {
    this.isDropdownOpen = false;
    this.router.navigate([path]);
  }
}
