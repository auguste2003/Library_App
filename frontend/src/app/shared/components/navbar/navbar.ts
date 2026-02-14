import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {
  authService = inject(AuthService);
  router = inject(Router);

  isMenuOpen = signal(false);
  isUserMenuOpen = signal(false);

  get currentUser() {
    return this.authService.currentUser();
  }

  get isAdmin() {
    return this.currentUser?.role === 'ADMIN';
  }

  get shouldShowNavbar() {
    const url = this.router.url;
    const hideOnRoutes = ['/setup', '/login', '/register', '/forgot-password', '/reset-password'];
    return !hideOnRoutes.some(route => url.startsWith(route));
  }

  toggleMenu() {
    this.isMenuOpen.update(v => !v);
  }

  toggleUserMenu() {
    this.isUserMenuOpen.update(v => !v);
  }

  closeMenus() {
    this.isMenuOpen.set(false);
    this.isUserMenuOpen.set(false);
  }

  logout() {
    this.authService.logout();
    this.closeMenus();
  }
}
