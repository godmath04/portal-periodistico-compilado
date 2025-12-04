export interface UserResponse {
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  active: boolean;
  roles: string[];
}

export interface AdminUserRequest {
  username: string;
  email: string;
  password?: string;
  firstName: string;
  lastName: string;
  roles: string[];
  active?: boolean;
}
