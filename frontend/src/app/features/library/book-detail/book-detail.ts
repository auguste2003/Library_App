import { Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '../../../core/services/book.service';
import { AuthService } from '../../../core/services/auth.service';
import { Book } from '../../../core/models/library.models';
import { DialogComponent } from '../../../shared/components/dialog/dialog';


@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [RouterLink, DialogComponent],
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.css'
})
export class BookDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private bookService = inject(BookService);
  public authService = inject(AuthService);

  @ViewChild(DialogComponent) dialog!: DialogComponent;

  book = signal<Book | null>(null);
  loading = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBook(Number(id));
    }
  }

  loadBook(id: number): void {
    this.loading.set(true);
    this.bookService.getBook(id).subscribe({
      next: (book) => {
        this.book.set(book);
        this.loading.set(false);
      },
      error: (err) => {
        this.dialog.open('Error', 'Book not found', 'error');
        this.loading.set(false);
      }
    });
  }

  borrowBook(): void {
    const currentBook = this.book();
    if (!currentBook || !currentBook.id) return;

    // Check if user is logged in
    if (!this.authService.isAuthenticated) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading.set(true);
    this.bookService.borrowBook(currentBook.id).subscribe({
      next: () => {
        this.dialog.open('Success', 'Book borrowed successfully!', 'success');
        this.loadBook(currentBook.id!); // Reload to update status
      },
      error: (err) => {
        const errorMessage = err.error?.error || 'Failed to borrow book';
        this.dialog.open('Error', errorMessage, 'error');
        this.loading.set(false);
      }
    });
  }
}
