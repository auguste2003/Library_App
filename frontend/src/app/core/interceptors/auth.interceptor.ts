import { HttpErrorResponse, HttpInterceptorFn, HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    // Add withCredentials to all requests to ensure cookies are sent
    const authReq = req.clone({
        withCredentials: true
    });

    const router = inject(Router);

    return next(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
            // Handle 429 Too Many Requests
            if (error.status === 429) {
                router.navigate(['/too-many-requests']);
                return throwError(() => error);
            }

            // Check if error is 401 Unauthorized and request is not already a refresh attempt
            if (error.status === 401 && !req.url.includes('/refresh') && !req.url.includes('/login')) {
                const http = inject(HttpClient);
                const authService = inject(AuthService);

                // Attempt to refresh token
                return http.post('/api/auth/refresh', {}, { withCredentials: true }).pipe(
                    switchMap(() => {
                        // If refresh successful, retry original request
                        return next(authReq);
                    }),
                    catchError((refreshError) => {
                        // If refresh fails, logout
                        authService.logout();
                        return throwError(() => error);
                    })
                );
            }
            return throwError(() => error);
        })
    );
};
