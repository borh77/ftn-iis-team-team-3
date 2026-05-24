import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserRole } from './auth.models';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }

  const requiredRoles = (route.data['roles'] as UserRole[] | undefined) ?? [];
  if (requiredRoles.length > 0 && !authService.hasAnyRole(requiredRoles)) {
    return router.createUrlTree(['/login']);
  }

  return true;
};
