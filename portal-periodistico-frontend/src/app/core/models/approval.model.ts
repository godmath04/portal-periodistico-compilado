export interface ArticleApproval {
  articleApprovalId: number;
  articleId: number;
  approverUserId: number;
  roleId: number;
  status: 'APPROVED' | 'REJECTED';
  comments?: string;
  timestamp: string;
}

export interface ApprovalRequest {
  articleId: number;
  status: 'APPROVED' | 'REJECTED';
  comments?: string;
}

export interface ApprovalResponse {
  articleId: number;
  articleTitle: string;
  approverUsername: string;
  roleName: string;
  approvalWeight: number;
  status: string;
  currentApprovalPercentage: number;
  articleStatus: string;
  message: string;
}
