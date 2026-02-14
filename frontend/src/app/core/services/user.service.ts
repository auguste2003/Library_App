import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiUrl}/users`;
    private http = inject(HttpClient);

    getAllUsers(): Observable<User[]> {
        return this.http.get<User[]>(this.apiUrl);
    }

    updateUserRole(id: number, role: 'USER' | 'ADMIN'): Observable<User> {
        return this.http.patch<User>(`${this.apiUrl}/${id}/role`, { role });
    }
}
