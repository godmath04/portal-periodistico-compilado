import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SuggestionService {
  // URL del microservicio
  private apiUrl = 'http://localhost:8083/api/suggestions/generate';

  constructor(private http: HttpClient) {}

  getSuggestions(): Observable<string> {
    // Solicitamos texto plano para parsearlo manualmente y evitar errores
    return this.http.get(this.apiUrl, { responseType: 'text' });
  }
}
