import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Calendar as CalendarService } from '../../core/services/calendar';
import { CalendarEntryResponse } from '../../core/models/calendar.model';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { Network, ContentType, PostStatus } from '../../core/models/enums';

@Component({
  selector: 'app-calendar',
  imports: [MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss',
})
export class Calendar implements OnInit {
  private readonly calendarService = inject(CalendarService);
  private readonly session = inject(Session);

  protected readonly entries = signal<CalendarEntryResponse[] | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly generating = signal(false);
  protected readonly generateMessage = signal<string | null>(null);

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
    this.loadCalendar();
  }

  protected loadCalendar(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.calendarService.getCalendar(userEmail).subscribe({
      next: (data) => {
        this.entries.set(data);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
  }

  protected generateContent(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.generating.set(true);
    this.generateMessage.set(null);

    this.calendarService.generateCalendarContent(userEmail).subscribe({
      next: () => {
        this.generating.set(false);
        this.generateMessage.set('Contenu du mois généré avec succès.');
        this.loadCalendar();
      },
      error: (error: HttpErrorResponse) => {
        this.generating.set(false);
        this.generateMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected networkLabel(network: Network | string): string {
    return this.networkLabels[network] ?? network;
  }

  protected contentTypeLabel(type: ContentType | string | null): string {
    if (!type) return '—';
    return this.contentTypeLabels[type] ?? type;
  }

  protected statusLabel(status: PostStatus | string): string {
    return this.statusLabels[status] ?? status;
  }

  protected formatDate(dateStr: string): { weekday: string; day: string } {
    const date = new Date(dateStr + 'T00:00:00');
    const weekday = date.toLocaleDateString('fr-FR', { weekday: 'short' });
    const day = date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
    return { weekday, day };
  }

  protected isToday(dateStr: string): boolean {
    const today = new Date();
    const d = new Date(dateStr + 'T00:00:00');
    return d.toDateString() === today.toDateString();
  }
}
