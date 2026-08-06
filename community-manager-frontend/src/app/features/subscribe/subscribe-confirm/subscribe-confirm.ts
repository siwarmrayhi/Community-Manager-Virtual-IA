import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { Subscription as SubscriptionService } from '../../../core/services/subscription';
import { Session } from '../../../core/state/session';
import { getHttpErrorMessage } from '../../../core/utils/http-error.util';

type ConfirmState = 'confirming' | 'success' | 'error';

@Component({
  selector: 'app-subscribe-confirm',
  imports: [MatIconModule, MatProgressSpinnerModule, MatButtonModule, RouterLink],
  templateUrl: './subscribe-confirm.html',
  styleUrl: './subscribe-confirm.scss',
})
export class SubscribeConfirm implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly session = inject(Session);

  protected readonly state = signal<ConfirmState>('confirming');
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const userEmail = this.session.userEmail();
    // PayPal ajoute ce paramètre à l'URL de retour une fois l'abonnement approuvé.
    const paypalSubscriptionId = this.route.snapshot.queryParamMap.get('subscription_id');

    if (!userEmail) {
      this.router.navigateByUrl('/login');
      return;
    }

    if (!paypalSubscriptionId) {
      this.state.set('error');
      this.errorMessage.set("Aucun identifiant d'abonnement PayPal reçu. Le paiement n'a peut-être pas abouti.");
      return;
    }

    this.subscriptionService.confirmerAbonnement(userEmail, paypalSubscriptionId).subscribe({
      next: () => this.state.set('success'),
      error: (error: HttpErrorResponse) => {
        this.state.set('error');
        this.errorMessage.set(getHttpErrorMessage(error));
      },
    });
  }

  protected goToDashboard(): void {
    this.router.navigateByUrl('/app/dashboard');
  }
}
