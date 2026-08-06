import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResponse, StatisticsResponse } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root',
})
export class Dashboard {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getDashboard(userEmail: string): Observable<DashboardResponse> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.get<DashboardResponse>(`${this.baseUrl}/dashboard`, { params });
  }

  /** GET /statistiques — statistiques détaillées de la bibliothèque */
  getStatistics(userEmail: string): Observable<StatisticsResponse> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.get<StatisticsResponse>(`${this.baseUrl}/statistiques`, { params });
  }
}
