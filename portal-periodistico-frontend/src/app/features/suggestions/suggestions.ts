import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { SuggestionService } from '../../core/services/suggestion.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-suggestions',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './suggestions.html',
  styleUrls: ['./suggestions.scss'],
})
export class SuggestionsComponent {
  suggestions: string[] = [];
  loading = false;
  error = '';

  constructor(
    private suggestionService: SuggestionService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  getSuggestions(): void {
    this.loading = true;
    this.error = '';
    this.suggestions = [];
    this.cdr.detectChanges();

    this.suggestionService.getSuggestions().subscribe({
      next: (response) => {
        console.log('Raw response:', response);
        try {
          let cleanResponse = response.trim();
          cleanResponse = cleanResponse.replace(/```json\n?/g, '');
          cleanResponse = cleanResponse.replace(/```\n?/g, '');
          cleanResponse = cleanResponse.trim();

          console.log('Clean response:', cleanResponse);
          this.suggestions = JSON.parse(cleanResponse);
          console.log('Parsed suggestions:', this.suggestions);
        } catch (e) {
          this.error = 'Error al procesar las sugerencias';
          console.error('Error parsing suggestions:', e);
          console.error('Response was:', response);
        }

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Error al obtener sugerencias. Verifica que el servicio esté corriendo.';
        console.error('Error:', err);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      alert('Título copiado al portapapeles');
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
