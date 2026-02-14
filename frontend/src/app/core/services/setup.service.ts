import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticationResponse, RegisterRequest } from '../models/auth.models';

export interface SetupStatus {
    setupRequired: boolean;
}

// Reuse RegisterRequest since it has the same fields
export type SetupAdminRequest = RegisterRequest;

@Injectable({
    providedIn: 'root'
})
export class SetupService {
    private apiUrl = `${environment.apiUrl}/auth/setup`;

    constructor(private http: HttpClient) { }

    getSetupStatus(): Observable<SetupStatus> {
        return this.http.get<SetupStatus>(`${this.apiUrl}/status`);
    }

    createInitialAdmin(request: SetupAdminRequest): Observable<AuthenticationResponse> {
        return this.http.post<AuthenticationResponse>(`${this.apiUrl}/admin`, request);
    }
}
