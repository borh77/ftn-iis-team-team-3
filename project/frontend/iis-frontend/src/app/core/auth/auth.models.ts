export type UserRole = 'ROLE_ADMIN' | 'ROLE_PRICELIST_CREATOR' | 'ROLE_BUYER';

export interface JwtPayload {
  sub: string;
  roles: UserRole[];
  exp: number;
  hasChangedPassword?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  roles: UserRole[];
  active: boolean;
  hasChangedPassword: boolean;
}

export interface AuthSession {
  token: string;
  username: string;
  roles: UserRole[];
  active: boolean;
  hasChangedPassword: boolean;
}

export interface SpringPage<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface UserRow {
  id: number;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  role: UserRole;
  active: boolean;
  hasChangedPassword: boolean;
}

export interface CreateUserPayload {
  username: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface UpdateProfilePayload {
  email: string;
  firstName: string;
  lastName: string;
}

export interface PasswordChangePayload {
  oldPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}
