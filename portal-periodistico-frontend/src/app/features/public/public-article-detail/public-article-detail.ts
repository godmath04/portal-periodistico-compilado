import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ArticleService } from '../../../core/services/article.service';
import { Article } from '../../../core/models/article.model';

@Component({
  selector: 'app-public-article-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './public-article-detail.html',
  styleUrl: './public-article-detail.scss',
})
export class PublicArticleDetail implements OnInit {
  article: Article | null = null;
  loading: boolean = false;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private articleService: ArticleService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const articleId = this.route.snapshot.paramMap.get('id');
    if (articleId) {
      this.loadArticle(+articleId);
    } else {
      this.errorMessage = 'ID de artículo no válido';
    }
  }

  loadArticle(articleId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.articleService.getArticleById(articleId).subscribe({
      next: (article) => {
        if (article.status.idArticleStatus !== 2) {
          this.errorMessage = 'Este artículo no está disponible públicamente';
          this.loading = false;
          this.cdr.detectChanges();
          return;
        }
        this.article = article;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading article:', error);
        this.errorMessage = error.error?.error || 'Error al cargar el artículo';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  getAuthorName(): string {
    if (!this.article) return '';
    if (this.article.author.firstName && this.article.author.lastName) {
      return `${this.article.author.firstName} ${this.article.author.lastName}`;
    }
    return this.article.author.username || 'Autor desconocido';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }

  goBack(): void {
    this.router.navigate(['/public/articles']);
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
