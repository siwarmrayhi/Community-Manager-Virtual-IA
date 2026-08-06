import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ConfigurationRequest, ConfigurationResponse } from '../models/configuration.model';

@Injectable({
  providedIn: 'root',
})
export class Configuration {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  /** PUT /configuration — à appeler après POST /abonnement, avant /abonnement/confirmer */
  updateConfiguration(payload: ConfigurationRequest): Observable<ConfigurationResponse> {
    return this.http.put<ConfigurationResponse>(`${this.baseUrl}/configuration`, payload);
  }
}
