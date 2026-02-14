import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Book, Loan, Page } from '../models/library.models';

@Injectable({
    providedIn: 'root'
})
export class BookService {
    private apiUrl = `${environment.apiUrl}/library/books`;
    private loanUrl = `${environment.apiUrl}/library/loans`;

    constructor(private http: HttpClient) { }

    getBooks(page: number = 0, size: number = 10, query?: string): Observable<Page<Book>> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size);

        if (query) {
            params = params.set('query', query);
        }

        return this.http.get<Page<Book>>(this.apiUrl, { params });
    }

    getBook(id: number): Observable<Book> {
        return this.http.get<Book>(`${this.apiUrl}/${id}`);
    }

    // Admin only
    createBook(book: Book): Observable<Book> {
        return this.http.post<Book>(this.apiUrl, book);
    }

    updateBook(id: number, book: Book): Observable<Book> {
        return this.http.put<Book>(`${this.apiUrl}/${id}`, book);
    }

    deleteBook(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    // Loans
    borrowBook(bookId: number): Observable<Loan> {
        const params = new HttpParams().set('bookId', bookId);
        return this.http.post<Loan>(this.loanUrl, {}, { params });
    }

    getMyLoans(page: number = 0, size: number = 10): Observable<Page<Loan>> {
        const params = new HttpParams()
            .set('page', page)
            .set('size', size);
        return this.http.get<Page<Loan>>(`${this.loanUrl}/my-loans`, { params });
    }

    returnBook(loanId: number): Observable<Loan> {
        return this.http.post<Loan>(`${this.loanUrl}/${loanId}/return`, {});
    }
}
