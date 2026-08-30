import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { Post as PostService } from '../../core/services/post';
import { Dashboard as DashboardService } from '../../core/services/dashboard';
import { PostResponse, PostFilters } from '../../core/models/post.model';
import { StatisticsResponse } from '../../core/models/dashboard.model';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { Network, ContentType, PostStatus } from '../../core/models/enums';
import { MatDialog } from '@angular/material/dialog';
import { GeneratePostDialog, GeneratePostDialogResult } from '../../shared/components/generate-post-dialog/generate-post-dialog';

@Component({
  selector: 'app-library',
  imports: [
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
  ],
  templateUrl: './library.html',
  styleUrl: './library.scss',
})
export class Library implements OnInit {
  private readonly postService = inject(PostService);
  private readonly dashboardService = inject(DashboardService);
  private readonly session = inject(Session);

  protected readonly posts = signal<PostResponse[]>([]);
  protected readonly statistics = signal<StatisticsResponse | null>(null);
  protected loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly generating = signal(false);
  protected readonly generateMessage = signal<string | null>(null);

  protected readonly editingId = signal<number | null>(null);
  protected readonly editText = signal('');
  protected readonly actionLoadingId = signal<number | null>(null);

  protected readonly filters = signal<PostFilters>({});

  protected readonly networks: Network[] = ['LINKEDIN', 'FACEBOOK', 'INSTAGRAM'];
  protected readonly contentTypes: ContentType[] = [
    'LISTING_PROMO',
    'EXPERT_ADVICE',
    'INDUSTRY_NEWS_COMMENTARY',
    'CLIENT_SUCCESS_STORY',
    'ENGAGING_CONTENT',
    'EVENT_PROMO',
  ];
  // ⚠️ Placeholder non confirmé, voir core/models/enums.ts
  protected readonly statuses: PostStatus[] = ['GENERATED', 'PENDING_VALIDATION', 'READY_TO_PUBLISH', 'REJECTED'];
  private readonly dialog = inject(MatDialog);
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

  private readonly statusLabels: Record<string, string> = {
    GENERATED: 'Généré',
    PENDING_VALIDATION: 'À valider',
    READY_TO_PUBLISH: 'Prêt à publier',
    REJECTED: 'Rejeté',
  };

  ngOnInit(): void {
    this.loadPosts();
    this.loadStatistics();
  }

  protected loadPosts(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.postService.getPosts(userEmail, this.filters()).subscribe({
      next: (data) => {
        this.posts.set(data);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
  }

  protected loadStatistics(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.dashboardService.getDashboard(userEmail).subscribe({
      next: (data) => this.statistics.set(data.statistics),
      error: () => {
        // Non bloquant : la bibliothèque reste utilisable même si les stats échouent.
      },
    });
  }

  protected updateFilter(key: keyof PostFilters, value: string): void {
    this.filters.update((f) => ({ ...f, [key]: value || undefined }));
    this.loadPosts();
  }

  protected resetFilters(): void {
    this.filters.set({});
    this.loadPosts();
  }

    protected generatePost(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    const dialogRef = this.dialog.open(GeneratePostDialog);

    dialogRef.afterClosed().subscribe((result: GeneratePostDialogResult | undefined) => {
      if (!result) return; // modale annulée

      this.generating.set(true);
      this.generateMessage.set(null);

      this.postService.generatePosts({ userEmail, ...result }).subscribe({
        next: () => {
          this.generating.set(false);
          this.loadPosts();
        },
        error: (error: HttpErrorResponse) => {
          this.generating.set(false);
          this.generateMessage.set(getHttpErrorMessage(error));
        },
      });
    });
  }

  protected isPending(post: PostResponse): boolean {
    return post.status === 'GENERATED' || post.status === 'PENDING_VALIDATION';
  }

  protected validate(post: PostResponse): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.actionLoadingId.set(post.id);
    this.postService.validatePost(post.id, userEmail).subscribe({
      next: (updated) => this.replacePost(updated),
      error: (error: HttpErrorResponse) => {
        this.actionLoadingId.set(null);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected startEdit(post: PostResponse): void {
    this.editingId.set(post.id);
    this.editText.set(post.text);
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
    this.editText.set('');
  }

  protected saveEdit(post: PostResponse): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.actionLoadingId.set(post.id);
    this.postService.modifyPost(post.id, { userEmail, text: this.editText() }).subscribe({
      next: (updated) => {
        this.replacePost(updated);
        this.cancelEdit();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoadingId.set(null);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected requestAlternative(post: PostResponse): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.actionLoadingId.set(post.id);
    this.postService.requestAlternative(post.id, { userEmail }).subscribe({
      next: () => {
        this.actionLoadingId.set(null);
        this.loadPosts(); // l'original reste inchangé, un nouveau post alternatif est créé
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoadingId.set(null);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected reuse(post: PostResponse): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.actionLoadingId.set(post.id);
    this.postService.reusePost(post.id, { userEmail }).subscribe({
      next: () => {
        this.actionLoadingId.set(null);
        this.loadPosts();
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoadingId.set(null);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected reject(post: PostResponse): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;
    if (!confirm('Rejeter définitivement ce post ?')) return;

    this.actionLoadingId.set(post.id);
    this.postService.rejectPost(post.id, userEmail).subscribe({
      next: () => {
        this.actionLoadingId.set(null);
        this.posts.update((list) => list.filter((p) => p.id !== post.id));
      },
      error: (error: HttpErrorResponse) => {
        this.actionLoadingId.set(null);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  private replacePost(updated: PostResponse): void {
    this.actionLoadingId.set(null);
    this.posts.update((list) => list.map((p) => (p.id === updated.id ? updated : p)));
  }

  protected networkLabel(network: Network | string): string {
    return this.networkLabels[network] ?? network;
  }

  protected contentTypeLabel(type: ContentType | string): string {
    return this.contentTypeLabels[type] ?? type;
  }

  protected statusLabel(status: PostStatus | string): string {
    return this.statusLabels[status] ?? status;
  }
}
