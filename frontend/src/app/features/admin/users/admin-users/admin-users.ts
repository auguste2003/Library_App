import { Component, inject, OnInit, OnDestroy, signal, computed, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../../../core/services/user.service';
import { User } from '../../../../core/models/auth.models';
import { AuthService } from '../../../../core/services/auth.service';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { DialogComponent } from '../../../../shared/components/dialog/dialog';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [ReactiveFormsModule, DialogComponent],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css',
})
export class AdminUsers implements OnInit, OnDestroy {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private destroy$ = new Subject<void>();

  @ViewChild(DialogComponent) dialog!: DialogComponent;

  allUsers = signal<User[]>([]);
  currentUserEmail = signal<string>('');

  // Reactive search control
  searchControl = new FormControl('');
  searchQuery = signal('');

  // Filtered users based on search
  users = computed(() => {
    const query = this.searchQuery().toLowerCase();
    if (!query) {
      return this.allUsers();
    }
    return this.allUsers().filter(user =>
      user.firstname?.toLowerCase().includes(query) ||
      user.lastname?.toLowerCase().includes(query) ||
      user.email?.toLowerCase().includes(query)
    );
  });

  ngOnInit() {
    this.currentUserEmail.set(this.authService.currentUser()?.sub || '');
    this.loadUsers();
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

  loadUsers() {
    this.userService.getAllUsers().subscribe(users => {
      this.allUsers.set(users);
    });
  }

  toggleRole(user: User) {
    if (user.email === this.currentUserEmail()) {
      this.dialog.open('Error', 'You cannot change your own role.', 'error');
      return;
    }

    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    const message = `Are you sure you want to change ${user.firstname}'s role to ${newRole}?`;

    this.dialog.open(
      'Confirm Role Change',
      message,
      'success',
      () => {
        this.userService.updateUserRole(user.id!, newRole).subscribe({
          next: () => {
            this.loadUsers();
            this.dialog.open('Success', 'User role updated successfully', 'success');
          },
          error: () => this.dialog.open('Error', 'Failed to update user role', 'error')
        });
      }
    );
  }
}
