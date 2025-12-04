import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse, AdminUserRequest } from '../models/admin-user.model';

@Injectable({
  providedIn: 'root',
})
export class AdminUserService {
  private apiUrl = 'http://localhost:8081/admin/users';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los usuarios
   */
  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.apiUrl);
  }

  /**
   * Crea un nuevo usuario
   */
  createUser(request: AdminUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.apiUrl, request);
  }

  /**
   * Actualiza un usuario existente
   */
  updateUser(id: number, request: AdminUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Elimina un usuario (soft delete)
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Obtiene todos los roles disponibles (helper)
   */
  getAvailableRoles(): string[] {
    return ['Reportero', 'Editor', 'Revisor Legal', 'Jefe de Redacción', 'Administrador'];
  }
}
