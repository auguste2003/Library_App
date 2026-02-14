import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const user = authService.currentUser();

    if (!user) {
        return router.createUrlTree(['/login']);
    }

    if (user.role !== 'ADMIN') {
        return router.createUrlTree(['/books']);
    }

    return true;
};
