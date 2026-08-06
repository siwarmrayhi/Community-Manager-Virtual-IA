import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CalendarEntryResponse } from '../models/calendar.model';

@Injectable({
  providedIn: 'root',
})
export class Calendar {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  /** GET /calendrier — vue éditoriale sur 14 jours */
  getCalendar(userEmail: string): Observable<CalendarEntryResponse[]> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.get<CalendarEntryResponse[]>(`${this.baseUrl}/calendrier`, { params });
  }

  /** POST /calendrier/generer — remplit les 30 prochains jours selon la fréquence configurée */
  generateCalendarContent(userEmail: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/calendrier/generer`, { userEmail });
  }
}
