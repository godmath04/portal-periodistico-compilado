import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { PromptConfigService } from '../../../core/services/prompt-config.service';
import { AuthService } from '../../../core/services/auth.service';
import { PromptConfig } from '../../../core/models/prompt-config.model';

@Component({
  selector: 'app-prompt-config',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './prompt-config.html',
  styleUrl: './prompt-config.scss',
})
export class PromptConfigComponent implements OnInit {
  promptTemplate: string = '';
  originalPromptTemplate: string = '';
  loading: boolean = false;
  saving: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
  lastUpdated: string = '';
  updatedBy: string = '';

  constructor(
    private promptConfigService: PromptConfigService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadPromptConfig();
  }

  loadPromptConfig(): void {
    this.loading = true;
    this.errorMessage = '';

    this.promptConfigService.getPromptConfig().subscribe({
      next: (config: PromptConfig) => {
        this.promptTemplate = config.promptTemplate;
        this.originalPromptTemplate = config.promptTemplate;
        this.lastUpdated = config.updatedAt || '';
        this.updatedBy = config.updatedBy || '';
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading prompt config:', error);
        this.errorMessage = 'Error al cargar la configuración del prompt';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  savePromptConfig(): void {
    if (!this.promptTemplate.trim()) {
      this.errorMessage = 'El prompt no puede estar vacío';
      return;
    }

    if (!this.promptTemplate.includes('{TRENDS}')) {
      this.errorMessage = 'El prompt debe contener el placeholder {TRENDS}';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.promptConfigService.updatePromptConfig({ promptTemplate: this.promptTemplate }).subscribe({
      next: (config: PromptConfig) => {
        this.successMessage = 'Prompt actualizado exitosamente';
        this.originalPromptTemplate = config.promptTemplate;
        this.lastUpdated = config.updatedAt || '';
        this.updatedBy = config.updatedBy || '';
        this.saving = false;
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (error) => {
        console.error('Error updating prompt config:', error);
        this.errorMessage = 'Error al actualizar la configuración del prompt';
        this.saving = false;
        this.cdr.detectChanges();
      },
    });
  }

  resetChanges(): void {
    this.promptTemplate = this.originalPromptTemplate;
    this.errorMessage = '';
    this.successMessage = '';
  }

  hasChanges(): boolean {
    return this.promptTemplate !== this.originalPromptTemplate;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
