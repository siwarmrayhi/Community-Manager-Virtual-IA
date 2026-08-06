import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Dashboard as DashboardService } from '../../core/services/dashboard';
import { DashboardResponse } from '../../core/models/dashboard.model';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { Network, ContentType } from '../../core/models/enums';

@Component({
  selector: 'app-dashboard',
  imports: [MatIconModule, MatButtonModule, MatProgressSpinnerModule, DecimalPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  protected readonly data = signal<DashboardResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  private readonly networkLabels: Record<string, string> = {
    LINKEDIN: 'LinkedIn',
    FACEBOOK: 'Facebook',
    INSTAGRAM: 'Instagram',
  };

  private readonly contentTypeLabels: Record<string, string> = {
    LISTING_PROMO: 'Annonce & opportunité',
    EXPERT_ADVICE: "Conseil d'expert",
    INDUSTRY_NEWS_COMMENTARY: 'Actualité commentée',
    CLIENT_SUCCESS_STORY: 'Succès client',
    ENGAGING_CONTENT: 'Contenu engageant',
    EVENT_PROMO: 'Événement',
  };

  // ⚠️ Valeurs exactes de quickActions non confirmées (champ tronqué dans le PDF).
  // Ces routes sont des suppositions raisonnables ; toute valeur inconnue reste sans action.
  private readonly quickActionRoutes: Record<string, string> = {
    generer_contenu: '/app/posts',
    voir_calendrier: '/app/calendrier',
    valider_posts: '/app/posts',
  };

  ngOnInit(): void {
    this.loadDashboard();
  }

  protected loadDashboard(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.dashboardService.getDashboard(userEmail).subscribe({
      next: (data) => {
        this.data.set(data);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
  }

  protected networkLabel(network: Network | string | null): string {
    if (!network) return '—';
    return this.networkLabels[network] ?? network;
  }

  protected contentTypeLabel(type: ContentType | string | null): string {
    if (!type) return '—';
    return this.contentTypeLabels[type] ?? type;
  }

  protected humanizeAction(action: string): string {
    const label = action.replace(/_/g, ' ');
    return label.charAt(0).toUpperCase() + label.slice(1);
  }

  protected runQuickAction(action: string): void {
    this.router.navigateByUrl(this.quickActionRoutes[action] ?? '/app/dashboard');
  }

  protected formatDate(dateStr: string): string {
    const date = new Date(dateStr + 'T00:00:00');
    return date.toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' });
  }
}
