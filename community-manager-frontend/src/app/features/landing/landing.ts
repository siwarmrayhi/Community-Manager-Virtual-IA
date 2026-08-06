import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  inject,
  signal,
  WritableSignal,
  ElementRef,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { Subscription as SubscriptionService } from '../../core/services/subscription';
import { ServiceDescriptionResponse } from '../../core/models/subscription.model';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-landing',
  imports: [MatButtonModule, MatProgressSpinnerModule, MatIconModule],
  templateUrl: './landing.html',
  styleUrl: './landing.scss',
})
export class Landing implements OnInit, AfterViewInit, OnDestroy {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  protected readonly service = signal<ServiceDescriptionResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly showStickyBar = signal(false);

  // Compteurs animés du hero — toujours dérivés des vraies données API, jamais de chiffres inventés.
  protected readonly networkCount = signal(0);
  protected readonly contentTypeCount = signal(0);

  private revealObserver?: IntersectionObserver;
  private readonly prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  private readonly onScroll = (): void => {
    this.showStickyBar.set(window.scrollY > 420);
  };

  private readonly networkLabels: Record<string, string> = {
    LINKEDIN: 'LinkedIn',
    FACEBOOK: 'Facebook',
    INSTAGRAM: 'Instagram',
  };

  // Specs réelles par réseau, telles que définies dans le cahier des charges (section 3).
  private readonly networkDetails: Record<string, string> = {
    LINKEDIN: 'Ton pro · ~1500 caractères · hashtags sectoriels',
    FACEBOOK: 'Ton accessible · ~500 caractères · émoticônes',
    INSTAGRAM: 'Texte court ~150 caractères · hashtags nombreux',
  };

  private readonly contentTypeLabels: Record<string, string> = {
    LISTING_PROMO: 'Annonce & opportunité',
    EXPERT_ADVICE: "Conseil d'expert",
    INDUSTRY_NEWS_COMMENTARY: 'Actualité commentée',
    CLIENT_SUCCESS_STORY: 'Succès client',
    ENGAGING_CONTENT: 'Contenu engageant',
    EVENT_PROMO: 'Événement',
  };

  private readonly contentTypeIcons: Record<string, string> = {
    LISTING_PROMO: 'campaign',
    EXPERT_ADVICE: 'lightbulb',
    INDUSTRY_NEWS_COMMENTARY: 'article',
    CLIENT_SUCCESS_STORY: 'verified',
    ENGAGING_CONTENT: 'forum',
    EVENT_PROMO: 'event',
  };

  ngOnInit(): void {
    this.loadService();
    window.addEventListener('scroll', this.onScroll, { passive: true });
  }

  ngAfterViewInit(): void {
    this.revealObserver = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            this.revealObserver?.unobserve(entry.target);
          }
        }
      },
      { threshold: 0.15 }
    );
    this.observeRevealTargets();
  }

  ngOnDestroy(): void {
    window.removeEventListener('scroll', this.onScroll);
    this.revealObserver?.disconnect();
  }

  private observeRevealTargets(): void {
    setTimeout(() => {
      const targets = this.elementRef.nativeElement.querySelectorAll('.reveal');
      targets.forEach((target:any) => this.revealObserver?.observe(target));
    }, 0);
  }

  protected loadService(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.subscriptionService.getServiceDescription().subscribe({
      next: (data) => {
        this.service.set(data);
        this.loading.set(false);
        this.animateCount(data.includedNetworks.length, this.networkCount);
        this.animateCount(data.contentTypes.length, this.contentTypeCount);
        this.observeRevealTargets();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(getHttpErrorMessage(error));
        this.loading.set(false);
      },
    });
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

  protected onSubscribe(): void {
    this.router.navigate(['/subscribe']);
  }

  protected splitWords(text: string): string[] {
    return text.split(' ');
  }

  protected networkLabel(network: string): string {
    return this.networkLabels[network] ?? network;
  }

  protected networkDetail(network: string): string {
    return this.networkDetails[network] ?? '';
  }

  protected contentTypeLabel(type: string): string {
    return this.contentTypeLabels[type] ?? type;
  }

  protected contentTypeIcon(type: string): string {
    return this.contentTypeIcons[type] ?? 'auto_awesome';
  }
}
