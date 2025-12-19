import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';

@Injectable({
  providedIn: 'root',
})
export class SuggestionService {
  // URL del microservicio
  private apiUrl = `${environment.apiUrls.suggestion}/api/suggestions/generate`;

  constructor(private http: HttpClient) {}

  getSuggestions(): Observable<string> {
    // Solicitamos texto plano para parsearlo manualmente y evitar errores
    return this.http.get(this.apiUrl, { responseType: 'text' });
  }
}
