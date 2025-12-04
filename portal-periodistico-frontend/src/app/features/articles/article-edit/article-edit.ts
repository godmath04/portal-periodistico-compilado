import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { ArticleService } from '../../../core/services/article.service';
import { AuthService } from '../../../core/services/auth.service';
import { Article } from '../../../core/models/article.model';

@Component({
  selector: 'app-article-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive],
  templateUrl: './article-edit.html',
  styleUrl: './article-edit.scss',
})
export class ArticleEdit implements OnInit {
  articleForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  articleId!: number;
  article: Article | null = null;

  constructor(
    private fb: FormBuilder,
    private articleService: ArticleService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Obtener el ID del artículo desde la ruta
    this.articleId = Number(this.route.snapshot.paramMap.get('id'));

    this.initForm();
    this.loadArticle();
  }

  initForm(): void {
    this.articleForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      content: ['', [Validators.required, Validators.minLength(20)]],
    });
  }

  loadArticle(): void {
    this.isLoading = true;
    const userId = this.authService.getUserId();

    console.log('=== DEBUG LOAD ARTICLE ===');
    console.log('articleId:', this.articleId);
    console.log('userId:', userId);

    if (!userId) {
      this.errorMessage = 'Usuario no autenticado';
      this.isLoading = false;
      return;
    }

    // Cargar todos los artículos del usuario y buscar el que necesitamos
    this.articleService.getArticlesByAuthor(userId).subscribe({
      next: (articles) => {
        console.log('Artículos recibidos:', articles);
        console.log('Buscando artículo con ID:', this.articleId);

        this.article = articles.find((a) => a.idArticle === this.articleId) || null;

        console.log('Artículo encontrado:', this.article);

        if (!this.article) {
          this.errorMessage = 'Artículo no encontrado';
          this.isLoading = false;
          return;
        }

        // Verificar que el artículo se puede editar
        const editableStatuses = [1, 4]; // Borrador u Observado
        console.log('Estado del artículo:', this.article.status.idArticleStatus);
        console.log(
          '¿Es editable?',
          editableStatuses.includes(this.article.status.idArticleStatus)
        );

        if (!editableStatuses.includes(this.article.status.idArticleStatus)) {
          this.errorMessage =
            'Este artículo no se puede editar. Solo se pueden editar artículos en estado Borrador u Observado.';
          this.isLoading = false;
          return;
        }

        // Cargar los datos en el formulario
        this.articleForm.patchValue({
          title: this.article.title,
          content: this.article.content,
        });

        console.log('Formulario cargado con:', this.articleForm.value);
        console.log('Cambiando isLoading a false');
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar artículo:', error);
        this.errorMessage = 'Error al cargar el artículo';
        this.isLoading = false;
      },
    });
  }

  onSubmit(): void {
    if (this.articleForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const articleData = {
        title: this.articleForm.value.title,
        content: this.articleForm.value.content,
      };

      this.articleService.updateArticle(this.articleId, articleData).subscribe({
        next: (response) => {
          console.log('Artículo actualizado exitosamente:', response);
          this.router.navigate(['/articles']);
        },
        error: (error) => {
          console.error('Error al actualizar artículo:', error);
          this.errorMessage =
            error.error || 'Error al actualizar el artículo. Por favor, intenta nuevamente.';
          this.isLoading = false;
        },
      });
    } else {
      this.markFormGroupTouched(this.articleForm);
    }
  }

  onCancel(): void {
    this.router.navigate(['/articles']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach((key) => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  get title() {
    return this.articleForm.get('title');
  }

  get content() {
    return this.articleForm.get('content');
  }
}
