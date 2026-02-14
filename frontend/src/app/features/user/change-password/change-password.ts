import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [ReactiveFormsModule],
    templateUrl: './change-password.html',
    styleUrl: './change-password.css'
})
export class ChangePasswordComponent {
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);
    router = inject(Router);

    changePasswordForm: FormGroup;
    errorMessage: string = '';
    successMessage: string = '';

    constructor() {
        this.changePasswordForm = this.fb.group({
            currentPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', [Validators.required]]
        }, { validators: this.passwordMatchValidator });
    }

    passwordMatchValidator(form: FormGroup) {
        const newPassword = form.get('newPassword');
        const confirmPassword = form.get('confirmPassword');

        if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
            confirmPassword.setErrors({ passwordMismatch: true });
            return { passwordMismatch: true };
        }
        return null;
    }

    onSubmit(): void {
        if (this.changePasswordForm.valid) {
            const { currentPassword, newPassword, confirmPassword } = this.changePasswordForm.value;

            this.authService.changePassword(currentPassword, newPassword, confirmPassword).subscribe({
                next: () => {
                    this.successMessage = 'Password changed successfully!';
                    this.errorMessage = '';
                    this.changePasswordForm.reset();

                    // Redirect to profile or home after 2 seconds
                    setTimeout(() => {
                        this.router.navigate(['/books']);
                    }, 2000);
                },
                error: (err) => {
                    this.errorMessage = err.error?.message || 'Failed to change password. Please check your current password.';
                    this.successMessage = '';
                    console.error(err);
                }
            });
        }
    }
}
