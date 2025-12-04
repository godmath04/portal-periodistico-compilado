import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article } from '../models/article.model';
import { ApprovalRequest, ApprovalResponse } from '../models/approval.model';

@Injectable({
  providedIn: 'root',
})
export class ApprovalService {
  private apiUrl = 'http://localhost:8082/api/v1/approvals';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los artículos pendientes de aprobación para el usuario actual
   */
  getPendingApprovals(): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/pending`);
  }

  /**
   * Aprueba o rechaza un artículo
   */
  processApproval(articleId: number, request: ApprovalRequest): Observable<ApprovalResponse> {
    return this.http.post<ApprovalResponse>(this.apiUrl, {
      articleId: articleId,
      status: request.status,
      comments: request.comments,
    });
  }

  /**
   * Obtiene el historial de aprobaciones de un artículo
   */
  getApprovalHistory(articleId: number): Observable<ApprovalResponse[]> {
    return this.http.get<ApprovalResponse[]>(`${this.apiUrl}/article/${articleId}`);
  }
}
