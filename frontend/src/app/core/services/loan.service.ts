import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Loan, Page } from '../models/library.models';

@Injectable({
    providedIn: 'root'
})
export class LoanService {
    private apiUrl = `${environment.apiUrl}/library/loans`;
    private http = inject(HttpClient);

    // Admin: Get all loans in the system
    findAllLoans(page: number = 0, size: number = 10): Observable<Page<Loan>> {
        const params = new HttpParams()
            .set('page', page)
            .set('size', size);
        return this.http.get<Page<Loan>>(this.apiUrl, { params });
    }

    // Return a book
    returnBook(loanId: number): Observable<Loan> {
        return this.http.post<Loan>(`${this.apiUrl}/${loanId}/return`, {});
    }

    // Get current user's loans
    getMyLoans(page: number = 0, size: number = 10): Observable<Page<Loan>> {
        const params = new HttpParams()
            .set('page', page)
            .set('size', size);
        return this.http.get<Page<Loan>>(`${this.apiUrl}/my-loans`, { params });
    }

    // Borrow a book
    borrowBook(bookId: number): Observable<Loan> {
        const params = new HttpParams().set('bookId', bookId);
        return this.http.post<Loan>(this.apiUrl, {}, { params });
    }
}
