import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);

  const logged = localStorage.getItem('logged') === '1';
  if (!logged) {
    router.navigateByUrl('/login');
    return false;
  }

  const role = (localStorage.getItem('role') || '').toUpperCase();

  if (role === 'ADMIN') return true;

  router.navigateByUrl('/mapa');
  return false;
};
