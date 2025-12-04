import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/auth';
  private tokenKey = 'auth_token';

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Realiza el login del usuario
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        this.setToken(response.token);
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  /**
   * Cierra la sesión del usuario
   */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Obtiene el token almacenado
   */
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Guarda el token en localStorage
   */
  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  /**
   * Verifica si existe un token
   */
  private hasToken(): boolean {
    return !!this.getToken();
  }

  /**
   * Verifica si el usuario está autenticado
   */
  isLoggedIn(): boolean {
    return this.hasToken();
  }

  /**
   * Decodifica el JWT y extrae el payload
   */
  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      // Decodificar base64 y manejar caracteres UTF-8 correctamente
      const jsonPayload = decodeURIComponent(
        atob(payload)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error decodificando token:', error);
      return null;
    }
  }

  /**
   * Obtiene el userId del token JWT
   */
  getUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    const decoded = this.decodeToken(token);
    return decoded?.userId || null;
  }

  /**
   * Obtiene el username del token JWT
   */
  getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;

    const decoded = this.decodeToken(token);
    return decoded?.sub || null;
  }

  /**
   * Obtiene los roles del token JWT
   */
  getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];

    const decoded = this.decodeToken(token);
    return decoded?.roles || [];
  }

  /**
   * Obtiene la información completa del usuario actual desde el token
   */
  getCurrentUser(): { userId: number; username: string; roles: { roleName: string }[] } | null {
    const token = this.getToken();
    if (!token) return null;

    const decoded = this.decodeToken(token);
    if (!decoded) return null;

    return {
      userId: decoded.userId,
      username: decoded.sub,
      roles: (decoded.roles || []).map((role: string) => ({ roleName: role })),
    };
  }

  /**
   * Obtiene información básica de un usuario por ID (endpoint público)
   */
  getUserBasicInfo(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/users/${userId}`);
  }
}
