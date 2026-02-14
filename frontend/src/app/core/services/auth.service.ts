import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, map, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest, User } from '../models/auth.models';
import { jwtDecode } from 'jwt-decode';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = `${environment.apiUrl}/auth`;
    private usersUrl = `${environment.apiUrl}/users`;

    // Signal for current user state
    currentUser = signal<User | null>(null);

    constructor(private http: HttpClient, private router: Router) {
        this.loadCurrentUser();
    }

    register(request: RegisterRequest): Observable<AuthenticationResponse> {
        return this.http.post<AuthenticationResponse>(`${this.apiUrl}/register`, request, { withCredentials: true }).pipe(
            tap(() => {
                this.loadCurrentUser();
            })
        );
    }

    login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
        return this.http.post<AuthenticationResponse>(`${this.apiUrl}/login`, request, { withCredentials: true }).pipe(
            tap(() => {
                this.loadCurrentUser();
            })
        );
    }

    forgotPassword(request: { email: string }): Observable<string> {
        // Backend returns a string message
        return this.http.post(`${this.apiUrl}/forgot-password`, request, { responseType: 'text' });
    }

    resetPassword(request: { token: string, newPassword: string }): Observable<string> {
        // Backend returns a string message
        return this.http.post(`${this.apiUrl}/reset-password`, request, { responseType: 'text' });
    }

    changePassword(currentPassword: string, newPassword: string, confirmationPassword: string): Observable<any> {
        return this.http.patch(`${this.apiUrl}/change-password`, {
            currentPassword,
            newPassword,
            confirmationPassword
        }, { withCredentials: true });
    }

    logout(): void {
        this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
            next: () => {
                this.currentUser.set(null);
                this.router.navigate(['/login']);
            },
            error: () => {
                // Force logout even if backend fails
                this.currentUser.set(null);
                this.router.navigate(['/login']);
            }
        });
    }

    get isAuthenticated(): boolean {
        return !!this.currentUser();
    }

    public loadCurrentUser(): void {
        this.http.get<User>(`${this.usersUrl}/me`, { withCredentials: true }).subscribe({
            next: (user) => {
                this.currentUser.set(user);
            },
            error: () => {
                this.currentUser.set(null);
            }
        });
    }
}
