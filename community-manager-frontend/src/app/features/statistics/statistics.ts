import { Component, OnInit, inject, signal, WritableSignal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Dashboard as DashboardService } from '../../core/services/dashboard';
import { StatisticsResponse } from '../../core/models/dashboard.model';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { Network, ContentType } from '../../core/models/enums';

interface StatusBar {
  key: string;
  label: string;
  count: number;
  percent: number;
}

@Component({
  selector: 'app-statistics',
  imports: [MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './statistics.html',
  styleUrl: './statistics.scss',
})
export class Statistics implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly session = inject(Session);
  private readonly prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  protected readonly stats = signal<StatisticsResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly generatedCount = signal(0);
  protected readonly validatedCount = signal(0);
  protected readonly rateCount = signal(0);

  protected readonly statusBars = signal<StatusBar[]>([]);

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

  // ⚠️ Libellés basés sur le placeholder PostStatus non confirmé (voir core/models/enums.ts).
  private readonly statusLabels: Record<string, string> = {
    GENERATED: 'Généré',
    PENDING_VALIDATION: 'À valider',
    READY_TO_PUBLISH: 'Prêt à publier',
    REJECTED: 'Rejeté',
  };

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.dashboardService.getStatistics(userEmail).subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
        this.animateCount(data.postsGenerated, this.generatedCount);
        this.animateCount(data.postsValidated, this.validatedCount);
        this.animateCount(Math.round(data.validationRate * 100), this.rateCount);
        this.buildStatusBars(data.postsByStatus);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
  }

  private buildStatusBars(postsByStatus: Record<string, number>): void {
    const total = Object.values(postsByStatus).reduce((sum, n) => sum + n, 0);
    const bars: StatusBar[] = Object.entries(postsByStatus)
      .map(([key, count]) => ({
        key,
        label: this.statusLabels[key] ?? key,
        count,
        percent: total > 0 ? Math.round((count / total) * 100) : 0,
      }))
      .sort((a, b) => b.count - a.count);
    this.statusBars.set(bars);
  }

  private animateCount(target: number, target_signal: WritableSignal<number>): void {
    if (this.prefersReducedMotion) {
      target_signal.set(target);
      return;
    }
    const duration = 700;
    const start = performance.now();
    const step = (now: number) => {
      const progress = Math.min((now - start) / duration, 1);
      target_signal.set(Math.round(progress * target));
      if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }

  protected networkLabel(network: Network | string | null): string {
    if (!network) return '—';
    return this.networkLabels[network] ?? network;
  }

  protected contentTypeLabel(type: ContentType | string | null): string {
    if (!type) return '—';
    return this.contentTypeLabels[type] ?? type;
  }
}
