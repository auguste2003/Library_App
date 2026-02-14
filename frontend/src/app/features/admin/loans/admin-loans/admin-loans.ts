import { Component, inject, OnInit, OnDestroy, signal, computed, ViewChild } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { LoanService } from '../../../../core/services/loan.service';
import { Loan, Page } from '../../../../core/models/library.models';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { DialogComponent } from '../../../../shared/components/dialog/dialog';

@Component({
  selector: 'app-admin-loans',
  standalone: true,
  imports: [DatePipe, NgClass, ReactiveFormsModule, DialogComponent],
  templateUrl: './admin-loans.html',
  styleUrl: './admin-loans.css',
})
export class AdminLoans implements OnInit, OnDestroy {
  private loanService = inject(LoanService);
  private destroy$ = new Subject<void>();

  @ViewChild(DialogComponent) dialog!: DialogComponent;

  allLoans = signal<Loan[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  pageSize = signal(10);
  loading = signal(false);

  // Reactive search control
  searchControl = new FormControl('');
  searchQuery = signal('');

  // Filtered loans based on search
  loans = computed(() => {
    const query = this.searchQuery().toLowerCase();
    if (!query) {
      return this.allLoans();
    }
    return this.allLoans().filter(loan =>
      loan.book?.title?.toLowerCase().includes(query) ||
      loan.book?.author?.toLowerCase().includes(query) ||
      loan.userEmail?.toLowerCase().includes(query)
    );
  });

  ngOnInit() {
    this.loadLoans();
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
    ).subscribe(query => {
      this.searchQuery.set(query || '');
    });
  }

  loadLoans() {
    this.loading.set(true);
    this.loanService.findAllLoans(this.currentPage(), this.pageSize()).subscribe((page: Page<Loan>) => {
      this.allLoans.set(page.content);
      this.totalElements.set(page.totalElements);
      this.loading.set(false);
    });
  }

  changePage(page: number) {
    this.currentPage.set(page);
    this.loadLoans();
  }

  returnBook(loanId: number) {
    this.dialog.open(
      'Confirm Return',
      'Mark this book as returned?',
      'success',
      () => {
        this.loanService.returnBook(loanId).subscribe({
          next: () => {
            this.loadLoans();
            this.dialog.open('Success', 'Book returned successfully', 'success');
          },
          error: () => this.dialog.open('Error', 'Failed to return book', 'error')
        });
      }
    );
  }
}
