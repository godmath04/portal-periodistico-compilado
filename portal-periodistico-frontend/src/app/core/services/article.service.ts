import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article, CreateArticleRequest, UpdateArticleRequest } from '../models/article.model';
import { environment } from '../../../environments/environment.prod';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  private apiUrl = `${environment.apiUrls.article}/api/v1/articles`;

  constructor(private http: HttpClient) {}

  getAllPublishedArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(this.apiUrl);
  }

  getArticlesByAuthor(authorId: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/author/${authorId}`);
  }

  createArticle(article: CreateArticleRequest): Observable<Article> {
    return this.http.post<Article>(this.apiUrl, article);
  }

  updateArticle(articleId: number, article: UpdateArticleRequest): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${articleId}`, article);
  }

  deleteArticle(articleId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${articleId}`, { responseType: 'text' });
  }

  getArticleById(articleId: number): Observable<Article> {
    return this.http.get<Article>(`${this.apiUrl}/${articleId}`);
  }

  sendArticleToReview(articleId: number): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${articleId}/send-to-review`, {});
  }

  getPendingArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/pending`);
  }

  getMyArticles(authorId: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/author/${authorId}`);
  }
}
