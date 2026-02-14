import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout';
import { AdminBooks } from './books/admin-books/admin-books';
import { AdminLoans } from './loans/admin-loans/admin-loans';
import { AdminUsers } from './users/admin-users/admin-users';

export const ADMIN_ROUTES: Routes = [
    {
        path: '',
        component: AdminLayoutComponent,
        children: [
            { path: 'books', component: AdminBooks },
            { path: 'loans', component: AdminLoans },
            { path: 'users', component: AdminUsers },
            { path: '', redirectTo: 'books', pathMatch: 'full' }
        ]
    }
];
