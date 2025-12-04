import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PromptConfig, UpdatePromptRequest } from '../models/prompt-config.model';
import { environment } from '../../../environments/environment.prod';

@Injectable({
  providedIn: 'root',
})
export class PromptConfigService {
  private apiUrl = `${environment.apiUrls.suggestion}/api/admin/prompt-config`;

  constructor(private http: HttpClient) {}

  getPromptConfig(): Observable<PromptConfig> {
    return this.http.get<PromptConfig>(this.apiUrl);
  }

  updatePromptConfig(request: UpdatePromptRequest): Observable<PromptConfig> {
    return this.http.put<PromptConfig>(this.apiUrl, request);
  }
}
