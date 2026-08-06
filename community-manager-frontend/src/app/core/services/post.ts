import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PostResponse,
  PostFilters,
  GeneratePostRequest,
  ModifyPostRequest,
  AlternativeRequest,
  ReusePostRequest,
} from '../models/post.model';

@Injectable({
  providedIn: 'root',
})
export class Post {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getPosts(userEmail: string, filters: PostFilters = {}): Observable<PostResponse[]> {
    let params = new HttpParams().set('userEmail', userEmail);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.network) params = params.set('network', filters.network);
    if (filters.from) params = params.set('from', filters.from);
    if (filters.to) params = params.set('to', filters.to);
    if (filters.status) params = params.set('status', filters.status);
    return this.http.get<PostResponse[]>(`${this.baseUrl}/posts`, { params });
  }

  generatePosts(payload: GeneratePostRequest): Observable<PostResponse[]> {
    return this.http.post<PostResponse[]>(`${this.baseUrl}/generer`, payload);
  }

  validatePost(id: number, userEmail: string): Observable<PostResponse> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.patch<PostResponse>(`${this.baseUrl}/posts/${id}/valider`, {}, { params });
  }

  modifyPost(id: number, payload: ModifyPostRequest): Observable<PostResponse> {
    return this.http.patch<PostResponse>(`${this.baseUrl}/posts/${id}/modifier`, payload);
  }

  requestAlternative(id: number, payload: AlternativeRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/posts/${id}/alternative`, payload);
  }

  reusePost(id: number, payload: ReusePostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/posts/${id}/reutiliser`, payload);
  }

  rejectPost(id: number, userEmail: string): Observable<void> {
    const params = new HttpParams().set('userEmail', userEmail);
    return this.http.delete<void>(`${this.baseUrl}/posts/${id}`, { params });
  }
}
