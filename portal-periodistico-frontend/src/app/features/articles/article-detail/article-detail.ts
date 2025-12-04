import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ArticleService } from '../../../core/services/article.service';
import { AuthService } from '../../../core/services/auth.service';
import { Article } from '../../../core/models/article.model';
import { ApprovalService } from '../../../core/services/approval.service';
import { ApprovalResponse } from '../../../core/models/approval.model';

@Component({
  selector: 'app-article-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './article-detail.html',
  styleUrl: './article-detail.scss',
})
export class ArticleDetail implements OnInit {
  article: Article | null = null;
  loading: boolean = false;
  errorMessage: string = '';
  approvalHistory: ApprovalResponse[] = [];
  loadingHistory: boolean = false;

  // Role checking
  isReporter: boolean = false;

  statusMap: { [key: number]: { name: string; class: string } } = {
    1: { name: 'Borrador', class: 'status-draft' },
    2: { name: 'Publicado', class: 'status-published' },
    3: { name: 'En Revisión', class: 'status-review' },
    4: { name: 'Observado', class: 'status-flagged' },
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private articleService: ArticleService,
    private approvalService: ApprovalService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.checkUserRole();

    const articleId = this.route.snapshot.paramMap.get('id');
    if (articleId) {
      this.loadArticle(+articleId);
      this.loadApprovalHistory(+articleId);
    } else {
      this.errorMessage = 'ID de artículo no válido';
    }
  }

  checkUserRole(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.roles && currentUser.roles.length > 0) {
      const userRole = currentUser.roles[0].roleName;
      this.isReporter = userRole === 'Reportero';
    }
  }

  loadArticle(articleId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.articleService.getArticleById(articleId).subscribe({
      next: (article) => {
        this.article = article;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando artículo:', error);
        this.errorMessage = error.error?.error || 'Error al cargar el artículo';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  getStatusInfo(statusId: number) {
    return this.statusMap[statusId] || { name: 'Desconocido', class: '' };
  }

  loadApprovalHistory(articleId: number): void {
    this.loadingHistory = true;

    this.approvalService.getApprovalHistory(articleId).subscribe({
      next: (history) => {
        console.log('Historial de aprobaciones:', history);
        this.approvalHistory = history;
        this.loadingHistory = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando historial:', error);
        this.loadingHistory = false;
        this.cdr.detectChanges();
      },
    });
  }

  getApprovalStatusClass(status: string): string {
    return status === 'APPROVED' ? 'approval-approved' : 'approval-rejected';
  }

  getApprovalStatusText(status: string): string {
    return status === 'APPROVED' ? 'Aprobado' : 'Rechazado';
  }

  goBack(): void {
    this.router.navigate(['/articles']);
  }

  editArticle(articleId: number): void {
    this.router.navigate(['/articles/edit', articleId]);
  }

  deleteArticle(articleId: number): void {
    if (confirm('¿Estás seguro de eliminar este artículo? Esta acción no se puede deshacer.')) {
      this.loading = true;
      this.errorMessage = '';

      this.articleService.deleteArticle(articleId).subscribe({
        next: () => {
          console.log('Artículo eliminado exitosamente');
          this.router.navigate(['/articles']);
        },
        error: (error) => {
          console.error('Error al eliminar artículo:', error);
          this.errorMessage = error.error || 'Error al eliminar el artículo';
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
    }
  }

  submitForReview(articleId: number): void {
    const confirmMessage = this.article?.status.idArticleStatus === 4
      ? '¿Estás seguro de reenviar este artículo a revisión?'
      : '¿Estás seguro de enviar este artículo a revisión?';

    if (confirm(confirmMessage)) {
      this.loading = true;
      this.errorMessage = '';

      this.articleService.sendArticleToReview(articleId).subscribe({
        next: (updatedArticle) => {
          console.log('Artículo enviado a revisión:', updatedArticle);
          this.loadArticle(articleId);
        },
        error: (error) => {
          console.error('Error al enviar a revisión:', error);
          this.errorMessage = error.error || 'Error al enviar el artículo a revisión';
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
    }
  }
}
