import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ServiceDescriptionResponse, SubscriptionResponse } from '../models/subscription.model';

@Injectable({
  providedIn: 'root',
})
export class Subscription {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  /** GET /service — description publique du service (page de présentation) */
  getServiceDescription(): Observable<ServiceDescriptionResponse> {
    return this.http.get<ServiceDescriptionResponse>(`${this.baseUrl}/service`);
  }

  /** POST /abonnement — initier l'abonnement PayPal */
  initierAbonnement(userEmail: string): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.baseUrl}/abonnement`, { userEmail });
  }

  /** POST /abonnement/confirmer — après retour PayPal (déclenche les 5 posts de bienvenue) */
  confirmerAbonnement(userEmail: string, paypalSubscriptionId: string): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${this.baseUrl}/abonnement/confirmer`, {
      userEmail,
      paypalSubscriptionId,
    });
  }

  /** DELETE /abonnement — se désabonner */
  seDesabonner(userEmail: string): Observable<void> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.delete<void>(`${this.baseUrl}/abonnement`, { params });
  }
}
