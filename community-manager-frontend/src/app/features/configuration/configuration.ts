import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Configuration as ConfigurationService } from '../../core/services/configuration';
import { Dashboard as DashboardService } from '../../core/services/dashboard';
import { Session } from '../../core/state/session';
import { getHttpErrorMessage } from '../../core/utils/http-error.util';
import { Network, Tone, Language, Frequency } from '../../core/models/enums';

@Component({
  selector: 'app-configuration',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './configuration.html',
  styleUrl: './configuration.scss',
})
export class Configuration implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly configurationService = inject(ConfigurationService);
  private readonly dashboardService = inject(DashboardService);
  private readonly session = inject(Session);

  protected readonly loading = signal(true);
  protected readonly hasConfiguration = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly networkError = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly saveMessage = signal<string | null>(null);

  protected readonly tones: Tone[] = ['PROFESSIONAL', 'APPROACHABLE', 'DYNAMIC', 'INSTITUTIONAL'];
  protected readonly languages: Language[] = ['FRENCH', 'ENGLISH', 'ARABIC', 'MULTILINGUAL'];
  protected readonly frequencies: Frequency[] = [
    'THREE_POSTS_PER_WEEK',
    'ONE_POST_PER_DAY',
    'TWO_POSTS_PER_DAY',
  ];

  private readonly toneLabels: Record<Tone, string> = {
    PROFESSIONAL: 'Professionnel',
    APPROACHABLE: 'Accessible',
    DYNAMIC: 'Dynamique',
    INSTITUTIONAL: 'Institutionnel',
  };

  private readonly languageLabels: Record<Language, string> = {
    FRENCH: 'Français',
    ENGLISH: 'Anglais',
    ARABIC: 'Arabe',
    MULTILINGUAL: 'Multilingue',
  };

  private readonly frequencyLabels: Record<Frequency, string> = {
    THREE_POSTS_PER_WEEK: '3 posts / semaine',
    ONE_POST_PER_DAY: '1 post / jour',
    TWO_POSTS_PER_DAY: '2 posts / jour',
  };

  protected readonly toneIcons: Record<Tone, string> = {
    PROFESSIONAL: 'business_center',
    APPROACHABLE: 'sentiment_satisfied',
    DYNAMIC: 'bolt',
    INSTITUTIONAL: 'account_balance',
  };

  protected readonly languageIcons: Record<Language, string> = {
    FRENCH: 'translate',
    ENGLISH: 'translate',
    ARABIC: 'translate',
    MULTILINGUAL: 'public',
  };

  protected readonly frequencyIcons: Record<Frequency, string> = {
    THREE_POSTS_PER_WEEK: 'calendar_view_week',
    ONE_POST_PER_DAY: 'today',
    TWO_POSTS_PER_DAY: 'repeat',
  };

  protected readonly networkSpecs: Record<Network, string> = {
    LINKEDIN: 'Ton pro · ~1500 caractères · hashtags sectoriels',
    FACEBOOK: 'Ton accessible · ~500 caractères · émoticônes',
    INSTAGRAM: 'Texte court ~150 caractères · hashtags nombreux',
  };

  protected readonly form = this.fb.group({
    linkedin: [false],
    facebook: [false],
    instagram: [false],
    tone: [''],
    language: [''],
    frequency: [''],
    sector: ['', Validators.required],
    keywords: [''],
    profileDescription: [''],
  });

  ngOnInit(): void {
    this.loadConfiguration();
  }

  protected loadConfiguration(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.dashboardService.getDashboard(userEmail).subscribe({
      next: (data) => {
        this.loading.set(false);
        if (!data.configuration) {
          this.hasConfiguration.set(false);
          return;
        }
        this.hasConfiguration.set(true);
        const c = data.configuration;
        this.form.patchValue({
          linkedin: c.targetNetworks.includes('LINKEDIN'),
          facebook: c.targetNetworks.includes('FACEBOOK'),
          instagram: c.targetNetworks.includes('INSTAGRAM'),
          tone: c.tone ?? '',
          language: c.language ?? '',
          frequency: c.frequency ?? '',
          sector: c.sector ?? '',
          keywords: (c.keywords ?? []).join(', '),
          profileDescription: c.profileDescription ?? '',
        });
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected toneLabel(t: Tone): string {
    return this.toneLabels[t];
  }

  protected languageLabel(l: Language): string {
    return this.languageLabels[l];
  }

  protected frequencyLabel(f: Frequency): string {
    return this.frequencyLabels[f];
  }

  protected selectTone(t: Tone): void {
    this.form.patchValue({ tone: this.form.controls.tone.value === t ? '' : t });
  }

  protected selectLanguage(l: Language): void {
    this.form.patchValue({ language: this.form.controls.language.value === l ? '' : l });
  }

  protected selectFrequency(f: Frequency): void {
    this.form.patchValue({ frequency: this.form.controls.frequency.value === f ? '' : f });
  }

  protected onSubmit(): void {
    const userEmail = this.session.userEmail();
    if (!userEmail) return;

    const selectedNetworks = this.getSelectedNetworks();
    if (selectedNetworks.length === 0) {
      this.networkError.set('Sélectionne au moins un réseau.');
      return;
    }
    this.networkError.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { tone, language, frequency, sector, keywords, profileDescription } = this.form.getRawValue();

    this.saving.set(true);
    this.saveMessage.set(null);
    this.errorMessage.set(null);

    this.configurationService
      .updateConfiguration({
        userEmail,
        targetNetworks: selectedNetworks,
        tone: tone ? (tone as Tone) : undefined,
        language: language ? (language as Language) : undefined,
        frequency: frequency ? (frequency as Frequency) : undefined,
        sector: sector!,
        keywords: keywords ? keywords.split(',').map((k) => k.trim()).filter(Boolean) : undefined,
        profileDescription: profileDescription || undefined,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.saveMessage.set('Configuration mise à jour avec succès.');
        },
        error: (error: HttpErrorResponse) => {
          this.saving.set(false);
          this.errorMessage.set(getHttpErrorMessage(error));
        },
      });
  }

  private getSelectedNetworks(): Network[] {
    const { linkedin, facebook, instagram } = this.form.getRawValue();
    const networks: Network[] = [];
    if (linkedin) networks.push('LINKEDIN');
    if (facebook) networks.push('FACEBOOK');
    if (instagram) networks.push('INSTAGRAM');
    return networks;
  }
}
