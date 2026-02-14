import { Component, inject, OnInit, signal } from '@angular/core';
import { LoanService } from '../../../core/services/loan.service';
import { Loan, Page } from '../../../core/models/library.models';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-my-loans',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './my-loans.html',
  styleUrl: './my-loans.css'
})
export class MyLoans implements OnInit {
  private loanService = inject(LoanService);

  loans = signal<Loan[]>([]);
  currentPage = signal(0);
  pageSize = signal(10);
  totalElements = signal(0);
  loading = signal(false);
  filterActive = signal<boolean | null>(null);

  // Make Math available in template
  Math = Math;

  ngOnInit() {
    this.loadMyLoans();
  }

  loadMyLoans() {
    this.loading.set(true);
    this.loanService.getMyLoans(this.currentPage(), this.pageSize()).subscribe((page: Page<Loan>) => {
      let loans = page.content;

      // Apply filter if set
      if (this.filterActive() !== null) {
        loans = loans.filter(loan => loan.returned !== this.filterActive());
      }

      this.loans.set(loans);
      this.totalElements.set(page.totalElements);
      this.loading.set(false);
    });
  }

  setFilter(active: boolean | null) {
    this.filterActive.set(active);
    this.currentPage.set(0);
    this.loadMyLoans();
  }

  changePage(page: number) {
    if (page >= 0 && page * this.pageSize() < this.totalElements()) {
      this.currentPage.set(page);
      this.loadMyLoans();
    }
  }
}
