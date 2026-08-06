import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SuggestionResponse } from '../models/suggestion.model';

@Injectable({
  providedIn: 'root',
})
export class Suggestion {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  /** GET /suggestions — sujets suggérés à partir des annonces publiées */
  getSuggestions(userEmail: string): Observable<SuggestionResponse[]> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.get<SuggestionResponse[]>(`${this.baseUrl}/suggestions`, { params });
  }
}
