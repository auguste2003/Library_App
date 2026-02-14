import { APP_INITIALIZER, Provider } from '@angular/core';
import { Router } from '@angular/router';
import { SetupService } from '../services/setup.service';
import { catchError, of, tap } from 'rxjs';

export function initializeApp(setupService: SetupService, router: Router) {
    return () => setupService.getSetupStatus().pipe(
        tap(status => {
            if (status.setupRequired) {
                // We can't use router here directly because app is initializing
                // Instead we can rely on a guard or simply handle this in the main component
                // But for now, let's just expose a global signal or service property
                // For simplicity, we'll handle redirection in the App Component or a specific Guard
            }
        }),
        catchError(() => of(null))
    );
}
