import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const user = auth.user();
  if (!user) {
    router.navigate(['/signin']);
    return false;
  }
  if (allowedRoles.includes(user.role)) return true;
  router.navigate(['/']);
  return false;
};
