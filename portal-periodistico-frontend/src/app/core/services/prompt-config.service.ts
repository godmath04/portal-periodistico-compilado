import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PromptConfig, UpdatePromptRequest } from '../models/prompt-config.model';

@Injectable({
  providedIn: 'root',
})
export class PromptConfigService {
  private apiUrl = 'http://localhost:8083/api/admin/prompt-config';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene la configuración actual del prompt
   */
  getPromptConfig(): Observable<PromptConfig> {
    return this.http.get<PromptConfig>(this.apiUrl);
  }

  /**
   * Actualiza la configuración del prompt
   */
  updatePromptConfig(request: UpdatePromptRequest): Observable<PromptConfig> {
    return this.http.put<PromptConfig>(this.apiUrl, request);
  }
}
