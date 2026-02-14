import { Component, inject, OnInit, signal, OnDestroy } from '@angular/core';
import { BookService } from '../../../core/services/book.service';
import { Book } from '../../../core/models/library.models';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css'
})
export class BookListComponent implements OnInit, OnDestroy {
  private bookService = inject(BookService);
  private destroy$ = new Subject<void>();

  books = signal<Book[]>([]);
  currentPage = signal(0);
  totalPages = signal(0);
  loading = signal(false);

  // Reactive search control
  searchControl = new FormControl('');

  ngOnInit(): void {
    this.loadBooks();
    this.setupReactiveSearch();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setupReactiveSearch(): void {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage.set(0);
      this.loadBooks();
    });
  }

  loadBooks(): void {
    this.loading.set(true);
    const query = this.searchControl.value || '';

    this.bookService.getBooks(this.currentPage(), 10, query).subscribe({
      next: (page) => {
        this.books.set(page.content);
        this.totalPages.set(page.totalPages);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading books', err);
        this.loading.set(false);
      }
    });
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(p => p + 1);
      this.loadBooks();
    }
  }

  prevPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadBooks();
    }
  }
}
