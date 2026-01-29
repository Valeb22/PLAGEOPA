import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const logged = localStorage.getItem('logged') === '1';

  if (!logged) {
    router.navigateByUrl('/login');
    return false;
  }
  return true;
};
