import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Auth } from '../../core/services/auth';
import { Session } from '../../core/state/session';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, MatIconModule, MatButtonModule],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  protected readonly session = inject(Session);

  protected readonly mobileNavOpen = signal(false);

  protected readonly navItems: NavItem[] = [
    { label: 'Dashboard', route: '/app/dashboard', icon: 'dashboard' },
    { label: 'Calendrier éditorial', route: '/app/calendrier', icon: 'calendar_month' },
    { label: 'Posts', route: '/app/posts', icon: 'article' },
    { label: 'Suggestions', route: '/app/suggestions', icon: 'auto_awesome' },
    { label: 'Configuration', route: '/app/configuration', icon: 'tune' },
    { label: 'Abonnement', route: '/app/abonnement', icon: 'credit_card' },
    { label: 'Statistiques', route: '/app/statistiques', icon: 'insights' },
  ];

  protected toggleMobileNav(): void {
    this.mobileNavOpen.update((v) => !v);
  }

  protected closeMobileNav(): void {
    this.mobileNavOpen.set(false);
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
