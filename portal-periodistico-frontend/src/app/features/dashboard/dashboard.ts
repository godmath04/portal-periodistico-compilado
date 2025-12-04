import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ArticleService } from '../../core/services/article.service';
import { ApprovalService } from '../../core/services/approval.service';
import { AuthService } from '../../core/services/auth.service';
import { Article } from '../../core/models/article.model';
import { ApprovalRequest, ApprovalResponse } from '../../core/models/approval.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  pendingArticles: Article[] = [];
  myArticles: Article[] = [];
  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // Información del usuario
  userRole: string = '';
  isReporter: boolean = false;
  isApprover: boolean = false;
  isAdmin: boolean = false;

  // Para rastrear qué artículos ya han sido revisados por el rol del usuario
  articleReviewedByRole: Map<number, boolean> = new Map();

  // Para el modal de aprobación/rechazo
  showModal: boolean = false;
  selectedArticle: Article | null = null;
  approvalAction: 'APPROVED' | 'REJECTED' = 'APPROVED';
  comments: string = '';

  constructor(
    private articleService: ArticleService,
    private approvalService: ApprovalService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.checkUserRole();
    this.loadDashboardContent();
  }

  checkUserRole(): void {
    const currentUser = this.authService.getCurrentUser();
    console.log('Current user:', currentUser);

    if (currentUser && currentUser.roles && currentUser.roles.length > 0) {
      this.userRole = currentUser.roles[0].roleName;
      console.log('User role:', this.userRole);

      this.isReporter = this.userRole === 'Reportero';
      this.isApprover = ['Editor', 'Revisor Legal', 'Jefe de Redacción'].includes(this.userRole);
      this.isAdmin = this.userRole === 'Administrador';

      console.log('Is Reporter:', this.isReporter);
      console.log('Is Approver:', this.isApprover);
      console.log('Is Admin:', this.isAdmin);
    }
  }

  loadDashboardContent(): void {
    if (this.isReporter) {
      this.loadMyArticles();
    } else if (this.isApprover) {
      this.loadPendingArticles();
    }
  }

  loadMyArticles(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.errorMessage = 'Usuario no autenticado';
      this.loading = false;
      return;
    }

    this.articleService.getMyArticles(currentUser.userId).subscribe({
      next: (articles) => {
        console.log('Mis artículos:', articles);
        this.myArticles = articles;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando mis artículos:', error);
        this.errorMessage = 'Error al cargar tus artículos';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadPendingArticles(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.articleService.getPendingArticles().subscribe({
      next: (articles) => {
        console.log('Artículos pendientes:', articles);
        this.pendingArticles = articles;

        // Cargar el historial de aprobaciones para cada artículo
        this.checkApprovalHistory();
      },
      error: (error) => {
        console.error('Error cargando artículos pendientes:', error);
        this.errorMessage = 'Error al cargar los artículos pendientes';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  checkApprovalHistory(): void {
    if (this.pendingArticles.length === 0) {
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    let completedRequests = 0;
    const totalRequests = this.pendingArticles.length;

    this.pendingArticles.forEach(article => {
      this.approvalService.getApprovalHistory(article.idArticle).subscribe({
        next: (history: ApprovalResponse[]) => {
          // Verificar si el rol del usuario actual ya revisó este artículo
          const hasReviewed = history.some(approval =>
            approval.roleName === this.userRole
          );
          this.articleReviewedByRole.set(article.idArticle, hasReviewed);

          completedRequests++;
          if (completedRequests === totalRequests) {
            this.loading = false;
            this.cdr.detectChanges();
          }
        },
        error: (error) => {
          console.error('Error cargando historial de artículo:', article.idArticle, error);
          this.articleReviewedByRole.set(article.idArticle, false);

          completedRequests++;
          if (completedRequests === totalRequests) {
            this.loading = false;
            this.cdr.detectChanges();
          }
        }
      });
    });
  }

  openApprovalModal(article: Article, action: 'APPROVED' | 'REJECTED'): void {
    this.selectedArticle = article;
    this.approvalAction = action;
    this.comments = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedArticle = null;
    this.comments = '';
  }

  submitApproval(): void {
    if (!this.selectedArticle) return;

    const request: ApprovalRequest = {
      articleId: this.selectedArticle.idArticle,
      status: this.approvalAction,
      comments: this.comments || undefined,
    };

    this.loading = true;
    this.errorMessage = '';

    this.approvalService.processApproval(this.selectedArticle.idArticle, request).subscribe({
      next: (response) => {
        console.log('Aprobación procesada:', response);
        this.successMessage = response.message || 'Aprobación procesada exitosamente';
        this.closeModal();
        this.loadPendingArticles();
      },
      error: (error) => {
        console.error('Error procesando aprobación:', error);
        this.errorMessage = error.error || 'Error al procesar la aprobación';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  viewArticle(articleId: number): void {
    this.router.navigate(['/articles/detail', articleId]);
  }

  editArticle(articleId: number): void {
    this.router.navigate(['/articles/edit', articleId]);
  }

  hasUserRoleReviewedArticle(articleId: number): boolean {
    return this.articleReviewedByRole.get(articleId) || false;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
