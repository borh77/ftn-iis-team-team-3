import { UserRole } from './auth.models';

export function roleLabel(role: UserRole | string | null | undefined): string {
  switch (role) {
    case 'ROLE_ADMIN':
      return 'Administrator';
    case 'ROLE_PORTFOLIO_MANAGER':
      return 'Portfolio manager';
    case 'ROLE_PRICELIST_CREATOR':
      return 'Pricelist creator';
    case 'ROLE_BUYER':
      return 'Buyer';
    case 'ROLE_SALES_REPRESENTATIVE':
      return 'Sales representative';
    case 'ROLE_ACCOUNT_MANAGER':
      return 'Account manager';
    case 'ROLE_SALES_MANAGER':
      return 'Sales manager';
    case 'ROLE_LEKAR':
      return 'Doctor';
    case 'ROLE_PACIJENT':
      return 'Patient';
    case 'ROLE_FARMAKOVIGILANT':
      return 'Pharmacovigilance reviewer';
    default:
      return role ?? '';
  }
}
