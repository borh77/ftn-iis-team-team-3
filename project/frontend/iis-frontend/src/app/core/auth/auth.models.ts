export type UserRole =
  | 'ROLE_ADMIN'
  | 'ROLE_PRICELIST_CREATOR'
  | 'ROLE_BUYER'
  | 'ROLE_SALES_REPRESENTATIVE'
  | 'ROLE_ACCOUNT_MANAGER'
  | 'ROLE_SALES_MANAGER';

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
  firstName: string;
  lastName: string;
  password: string;
  role: UserRole;
}

export interface UpdateProfilePayload {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}

export interface ProfileUpdateResponse extends UserRow, LoginResponse {
}

export interface PasswordChangePayload {
  oldPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}
