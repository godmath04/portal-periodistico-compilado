export interface Article {
  idArticle: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt?: string;
  author: {
    idUser: number;
    username: string | null;
    firstName: string | null;
    lastName: string | null;
  };
  status: {
    idArticleStatus: number;
    statusName: string;
  };
}

export interface CreateArticleRequest {
  title: string;
  content: string;
}

export interface UpdateArticleRequest {
  title: string;
  content: string;
}
