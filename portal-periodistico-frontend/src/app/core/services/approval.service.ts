import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article } from '../models/article.model';
import { ApprovalRequest, ApprovalResponse } from '../models/approval.model';
import { environment } from '../../../environments/environment.prod';

@Injectable({
  providedIn: 'root',
})
export class ApprovalService {
  private apiUrl = `${environment.apiUrls.article}/api/v1/approvals`;

  constructor(private http: HttpClient) {}

  getPendingApprovals(): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/pending`);
  }

  processApproval(articleId: number, request: ApprovalRequest): Observable<ApprovalResponse> {
    return this.http.post<ApprovalResponse>(this.apiUrl, {
      articleId: articleId,
      status: request.status,
      comments: request.comments,
    });
  }

  getApprovalHistory(articleId: number): Observable<ApprovalResponse[]> {
    return this.http.get<ApprovalResponse[]>(`${this.apiUrl}/article/${articleId}`);
  }
}
