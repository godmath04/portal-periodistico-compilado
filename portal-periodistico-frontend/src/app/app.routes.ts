import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { ArticleList } from './features/articles/article-list/article-list';
import { ArticleCreate } from './features/articles/article-create/article-create';
import { ArticleEdit } from './features/articles/article-edit/article-edit';
import { SuggestionsComponent } from './features/suggestions/suggestions';
import { PublicArticleList } from './features/public/public-article-list/public-article-list';
import { PublicArticleDetail } from './features/public/public-article-detail/public-article-detail';
import { UserList } from './features/admin/user-list/user-list';
import { PromptConfigComponent } from './features/admin/prompt-config/prompt-config';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/public/articles',
    pathMatch: 'full',
  },
  {
    path: 'login',
    component: Login,
  },
  {
    path: 'public/articles',
    component: PublicArticleList,
  },
  {
    path: 'public/articles/:id',
    component: PublicArticleDetail,
  },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard],
  },
  {
    path: 'articles',
    component: ArticleList,
    canActivate: [authGuard],
  },
  {
    path: 'articles/create',
    component: ArticleCreate,
    canActivate: [authGuard],
  },
  {
    path: 'articles/edit/:id',
    component: ArticleEdit,
    canActivate: [authGuard],
  },
  {
    path: 'articles/detail/:id',
    loadComponent: () =>
      import('./features/articles/article-detail/article-detail').then((m) => m.ArticleDetail),
    canActivate: [authGuard],
  },
  {
    path: 'suggestions',
    component: SuggestionsComponent,
    canActivate: [authGuard],
  },
  {
    path: 'admin/users',
    component: UserList,
    canActivate: [authGuard],
  },
  {
    path: 'admin/prompt-config',
    component: PromptConfigComponent,
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '/login',
  },
];
