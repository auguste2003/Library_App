export interface Book {
    id?: number;
    isbn: string;
    title: string;
    author: string;
    borrowed: boolean;
    borrower?: string;
}

export interface Loan {
    id?: number;
    book: Book;
    userEmail: string;
    borrowDate: string;
    returnDate?: string;
    returned: boolean;
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // current page index
    first: boolean;
    last: boolean;
}
