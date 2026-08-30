import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Suggestion as SuggestionService } from '../../core/services/suggestion';
import { Post as PostService } from '../../core/services/post';
import { SuggestionResponse } from '../../core/models/suggestion.model';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { ContentType } from '../../core/models/enums';

@Component({
  selector: 'app-suggestions',
  imports: [MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './suggestions.html',
  styleUrl: './suggestions.scss',
})
export class Suggestions implements OnInit {
  private readonly suggestionService = inject(SuggestionService);
  private readonly postService = inject(PostService);
  private readonly session = inject(Session);

  protected readonly suggestions = signal<SuggestionResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly actionLoadingId = signal<number | null>(null);
  protected readonly generatedIds = signal<Set<number>>(new Set());
  protected readonly actionErrorMessage = signal<string | null>(null);

  private readonly contentTypeLabels: Record<string, string> = {
    LISTING_PROMO: 'Annonce & opportunité',
    EXPERT_ADVICE: "Conseil d'expert",
    INDUSTRY_NEWS_COMMENTARY: 'Actualité commentée',
    CLIENT_SUCCESS_STORY: 'Succès client',
    ENGAGING_CONTENT: 'Contenu engageant',
    EVENT_PROMO: 'Événement',
  };

  private readonly statusLabels: Record<string, string> = {
    NEW: 'Nouvelle',
    USED: 'Déjà utilisée',
    DISMISSED: 'Ignorée',
  };

  ngOnInit(): void {
    this.loadSuggestions();
  }

  protected loadSuggestions(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.suggestionService.getSuggestions(userEmail).subscribe({
      next: (data) => {
        this.suggestions.set(data);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
  }

  protected generateFromSuggestion(suggestion: SuggestionResponse): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.actionLoadingId.set(suggestion.id);

    this.postService
      .generatePosts({
        userEmail,
        contentType: suggestion.contentType,
        topic: suggestion.title,
        additionalInstructions: suggestion.description,
        plannedDate: suggestion.suggestedDate,
      })
      .subscribe({
        next: () => {
          this.actionLoadingId.set(null);
          this.generatedIds.update((set) => new Set(set).add(suggestion.id));
        },
            error: (error: HttpErrorResponse) => {
          this.actionLoadingId.set(null);
          this.actionErrorMessage.set(getHttpErrorMessage(error));
        },
      });
  }

  protected contentTypeLabel(type: ContentType | string): string {
    return this.contentTypeLabels[type] ?? type;
  }

  protected statusLabel(status: string): string {
    return this.statusLabels[status] ?? status;
  }

  protected isGenerated(id: number): boolean {
    return this.generatedIds().has(id);
  }
}
