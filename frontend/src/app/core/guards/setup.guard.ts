import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { SetupService } from '../services/setup.service';
import { map, catchError, of } from 'rxjs';

export const setupGuard: CanActivateFn = (route, state) => {
    const setupService = inject(SetupService);
    const router = inject(Router);

    return setupService.getSetupStatus().pipe(
        map(status => {
            // If setup IS required and we are NOT on the setup page, redirect to setup
            if (status.setupRequired && state.url !== '/setup') {
                return router.createUrlTree(['/setup']);
            }

            // If setup is NOT required and we ARE on the setup page, redirect to login
            if (!status.setupRequired && state.url === '/setup') {
                return router.createUrlTree(['/login']);
            }

            return true;
        }),
        catchError(() => {
            // If error (e.g. service down), allow navigation or handle gracefully
            // For now, allow navigation to proceed if not on setup page
            return of(true);
        })
    );
};
