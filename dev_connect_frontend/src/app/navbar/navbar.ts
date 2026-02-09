import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-navbar',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  standalone: true,
})
export class Navbar {
  @Input() isAuthenticated: boolean = false;
  @Input() username: string | null = null;

  @Output() logout = new EventEmitter<void>();

  isDropdownOpen: boolean = false;
  searchQuery: string = '';
  selectedTechStack: string = '';

  techStacks: string[] = [
    'Angular',
    'React',
    'Vue',
    'Java',
    'Spring Boot',
    'Node.js',
    'Python',
    'Django',
    'TypeScript',
    'JavaScript',
    'PostgreSQL',
    'MongoDB',
    'Docker',
    'Kubernetes',
    'AWS',
    'Azure',
  ];

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

  onSearch(): void {
    const trimmed = this.searchQuery.trim();
    if (!trimmed) return;

    // General search for title, username, description, etc.
    this.router.navigate(['/browse-ideas'], { queryParams: { q: trimmed } });
    this.searchQuery = '';
  }

  onTechStackFilter(): void {
    if (!this.selectedTechStack) {
      // Navigate to browse-ideas without filter
      this.router.navigate(['/browse-ideas']);
      return;
    }
    // Filter by selected tech stack
    this.router.navigate(['/browse-ideas'], { queryParams: { tech: this.selectedTechStack } });
  }
}
