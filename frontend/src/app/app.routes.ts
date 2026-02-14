import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegisterComponent } from './features/auth/register/register';
import { BookListComponent } from './features/library/book-list/book-list';
import { BookDetailComponent } from './features/library/book-detail/book-detail';
import { SetupComponent } from './features/admin/setup/setup';
import { setupGuard } from './core/guards/setup.guard';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { ForgotPasswordComponent } from './features/auth/forgot-password/forgot-password';
import { ResetPasswordComponent } from './features/auth/reset-password/reset-password';
import { MyLoans } from './features/user/my-loans/my-loans';
import { ChangePasswordComponent } from './features/user/change-password/change-password';

export const routes: Routes = [
    { path: 'setup', component: SetupComponent, canActivate: [setupGuard] },
    { path: 'login', component: LoginComponent, canActivate: [setupGuard] },
    { path: 'register', component: RegisterComponent, canActivate: [setupGuard] },
    { path: 'forgot-password', component: ForgotPasswordComponent, canActivate: [setupGuard] },
    { path: 'reset-password', component: ResetPasswordComponent, canActivate: [setupGuard] },
    { path: 'books', component: BookListComponent, canActivate: [setupGuard, authGuard] },
    { path: 'books/:id', component: BookDetailComponent, canActivate: [setupGuard, authGuard] },
    { path: 'my-loans', component: MyLoans, canActivate: [setupGuard, authGuard] },
    { path: 'change-password', component: ChangePasswordComponent, canActivate: [setupGuard, authGuard] },
    {
        path: 'too-many-requests',
        loadComponent: () => import('./core/errors/too-many-requests/too-many-requests.component').then(m => m.TooManyRequestsComponent)
    },
    {
        path: 'admin',
        loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES),
        canActivate: [setupGuard, adminGuard]
    },
    { path: '', redirectTo: 'books', pathMatch: 'full' },
    { path: '**', redirectTo: 'books' }
];
