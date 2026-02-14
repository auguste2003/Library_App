import { Component, inject, OnInit, OnDestroy, signal, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookService } from '../../../../core/services/book.service';
import { Book } from '../../../../core/models/library.models';
import { DatePipe } from '@angular/common';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { DialogComponent } from '../../../../shared/components/dialog/dialog';

@Component({
  selector: 'app-admin-books',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, DialogComponent],
  templateUrl: './admin-books.html',
  styleUrl: './admin-books.css',
})
export class AdminBooks implements OnInit, OnDestroy {
  private bookService = inject(BookService);
  private fb = inject(FormBuilder);
  private destroy$ = new Subject<void>();

  @ViewChild(DialogComponent) dialog!: DialogComponent;

  books = signal<Book[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  pageSize = signal(10);

  // Reactive search control
  searchControl = new FormControl('');

  // Modal State
  isModalOpen = signal(false);
  isEditing = signal(false);
  currentBookId = signal<number | null>(null);

  bookForm: FormGroup = this.fb.group({
    title: ['', Validators.required],
    author: ['', Validators.required],
    isbn: ['', Validators.required],
    available: [true]
  });

  ngOnInit() {
    this.loadBooks();
    this.setupReactiveSearch();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setupReactiveSearch() {
    this.searchControl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.currentPage.set(0);
      this.loadBooks();
    });
  }

  loadBooks() {
    const query = this.searchControl.value || '';
    this.bookService.getBooks(this.currentPage(), this.pageSize(), query)
      .subscribe(page => {
        this.books.set(page.content);
        this.totalElements.set(page.totalElements);
      });
  }

  changePage(page: number) {
    this.currentPage.set(page);
    this.loadBooks();
  }

  // Modal Actions
  openCreateModal() {
    this.isEditing.set(false);
    this.currentBookId.set(null);
    this.bookForm.reset({ available: true });
    this.isModalOpen.set(true);
  }

  openEditModal(book: Book) {
    this.isEditing.set(true);
    this.currentBookId.set(book.id || null);
    this.bookForm.patchValue({
      title: book.title,
      author: book.author,
      isbn: book.isbn,
      available: !book.borrowed
    });
    this.isModalOpen.set(true);
  }

  closeModal() {
    this.isModalOpen.set(false);
  }

  saveBook() {
    if (this.bookForm.invalid) return;

    const formValue = this.bookForm.value;
    const payload: Book = {
      id: this.currentBookId() || undefined,
      title: formValue.title,
      author: formValue.author,
      isbn: formValue.isbn,
      borrowed: !formValue.available
    };

    if (this.isEditing() && this.currentBookId()) {
      this.bookService.updateBook(this.currentBookId()!, payload).subscribe({
        next: () => {
          this.loadBooks();
          this.closeModal();
          this.dialog.open('Success', 'Book updated successfully', 'success');
        },
        error: () => this.dialog.open('Error', 'Failed to update book', 'error')
      });
    } else {
      this.bookService.createBook(payload).subscribe({
        next: () => {
          this.loadBooks();
          this.closeModal();
          this.dialog.open('Success', 'Book created successfully', 'success');
        },
        error: () => this.dialog.open('Error', 'Failed to create book', 'error')
      });
    }
  }

  deleteBook(id: number) {
    this.dialog.open(
      'Confirm Deletion',
      'Are you sure you want to delete this book? This action cannot be undone.',
      'error',
      () => {
        this.bookService.deleteBook(id).subscribe({
          next: () => {
            this.loadBooks();
            this.dialog.open('Success', 'Book deleted successfully', 'success');
          },
          error: () => this.dialog.open('Error', 'Failed to delete book', 'error')
        });
      }
    );
  }
}
