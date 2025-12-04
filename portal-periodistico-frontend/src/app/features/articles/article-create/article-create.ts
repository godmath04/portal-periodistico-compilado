import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ArticleService } from '../../../core/services/article.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-article-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive],
  templateUrl: './article-create.html',
  styleUrl: './article-create.scss',
})
export class ArticleCreate implements OnInit {
  articleForm!: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private articleService: ArticleService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  initForm(): void {
    this.articleForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      content: ['', [Validators.required, Validators.minLength(20)]],
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

      this.articleService.createArticle(articleData).subscribe({
        next: (response) => {
          console.log('Artículo creado exitosamente:', response);
          this.router.navigate(['/articles']);
        },
        error: (error) => {
          console.error('Error al crear artículo:', error);
          this.errorMessage = 'Error al crear el artículo. Por favor, intenta nuevamente.';
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
