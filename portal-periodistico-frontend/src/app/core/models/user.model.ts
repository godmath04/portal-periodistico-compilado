export interface User {
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  isActive: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  roles: string[];
}