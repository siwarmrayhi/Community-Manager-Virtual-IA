import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription as SubscriptionService } from '../../core/services/subscription';
import { Dashboard as DashboardService } from '../../core/services/dashboard';
import { ServiceDescriptionResponse } from '../../core/models/subscription.model';
import { ConfigurationResponse } from '../../core/models/configuration.model';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { SubscriptionStatus, Network } from '../../core/models/enums';

@Component({
  selector: 'app-settings',
  imports: [RouterLink, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './settings.html',
  styleUrl: './settings.scss',
})
export class Settings implements OnInit {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly dashboardService = inject(DashboardService);
  private readonly session = inject(Session);

  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly service = signal<ServiceDescriptionResponse | null>(null);
  protected readonly status = signal<SubscriptionStatus | null>(null);
  protected readonly configuration = signal<ConfigurationResponse | null>(null);
  protected readonly cancelling = signal(false);

  private readonly networkLabels: Record<string, string> = {
    LINKEDIN: 'LinkedIn',
    FACEBOOK: 'Facebook',
    INSTAGRAM: 'Instagram',
  };

  private readonly statusLabels: Record<SubscriptionStatus, string> = {
    PENDING: 'En attente de confirmation',
    ACTIVE: 'Actif',
    CANCELLED: 'Résilié',
  };

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.subscriptionService.getServiceDescription().subscribe({
      next: (data) => this.service.set(data),
      error: () => {
        // Non bloquant : on peut afficher le statut même sans la fiche du service.
      },
    });

    this.dashboardService.getDashboard(userEmail).subscribe({
      next: (data) => {
        this.status.set(data.subscriptionStatus);
        this.configuration.set(data.configuration);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
  }

  protected statusLabel(status: SubscriptionStatus): string {
    return this.statusLabels[status];
  }

  protected networkLabel(network: Network | string): string {
    return this.networkLabels[network] ?? network;
  }

  protected unsubscribe(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;
    if (!confirm('Résilier ton abonnement ? Les générations automatiques s\'arrêteront.')) return;

    this.cancelling.set(true);
    this.errorMessage.set(null);

    this.subscriptionService.seDesabonner(userEmail).subscribe({
      next: () => {
        this.cancelling.set(false);
        this.status.set('CANCELLED');
      },
      error: (error: HttpErrorResponse) => {
        this.cancelling.set(false);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }
}
