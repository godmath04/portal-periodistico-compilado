import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Article, CreateArticleRequest, UpdateArticleRequest } from '../models/article.model';

@Injectable({
  providedIn: 'root',
})
export class ArticleService {
  private apiUrl = 'http://localhost:8082/api/v1/articles';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los artículos PUBLICADOS (público)
   */
  getAllPublishedArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(this.apiUrl);
  }

  /**
   * Obtiene los artículos de un autor específico
   * Usar esto para "mis artículos" pasando el userId del reportero
   */
  getArticlesByAuthor(authorId: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/author/${authorId}`);
  }

  /**
   * Crea un nuevo artículo (Borrador)
   * Requiere autenticación (token JWT)
   */
  createArticle(article: CreateArticleRequest): Observable<Article> {
    return this.http.post<Article>(this.apiUrl, article);
  }

  /**
   * Actualiza un artículo existente
   * Solo Borrador u Observado
   * Requiere autenticación (token JWT)
   */
  updateArticle(articleId: number, article: UpdateArticleRequest): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${articleId}`, article);
  }

  /**
   * Elimina un artículo
   * Solo Borrador u Observado
   * Requiere autenticación (token JWT)
   */
  deleteArticle(articleId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${articleId}`, { responseType: 'text' });
  }

  /**
   * Obtiene un artículo por su ID
   * Requiere autenticación (token JWT)
   */
  getArticleById(articleId: number): Observable<Article> {
    return this.http.get<Article>(`${this.apiUrl}/${articleId}`);
  }

  /**
   * Envía un artículo a revisión
   * Cambia el estado de Borrador → En Revisión
   * Requiere autenticación (token JWT)
   */
  sendArticleToReview(articleId: number): Observable<Article> {
    return this.http.put<Article>(`${this.apiUrl}/${articleId}/send-to-review`, {});
  }

  /**
   * Obtiene los artículos pendientes de aprobación (estado "En revisión")
   * Requiere autenticación (token JWT)
   */
  getPendingArticles(): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/pending`);
  }

  /**
   * Obtiene los artículos del usuario autenticado actual
   * Usa el endpoint de autor con el ID del usuario logueado
   */
  getMyArticles(authorId: number): Observable<Article[]> {
    return this.http.get<Article[]>(`${this.apiUrl}/author/${authorId}`);
  }
}
