import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  resetPasswordForm: FormGroup = this.fb.group({
    newPassword: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/)
    ]]
  });

  token = '';
  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'];
    if (!this.token) {
      this.errorMessage = 'Invalid or missing token.';
    }
  }

  onSubmit(): void {
    if (this.resetPasswordForm.valid && this.token) {
      this.isSubmitting = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.authService.resetPassword({
        token: this.token,
        newPassword: this.resetPasswordForm.get('newPassword')?.value
      }).subscribe({
        next: (response) => {
          this.successMessage = response || 'Password successfully reset.';
          this.isSubmitting = false;
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          this.errorMessage = 'Failed to reset password. Token might be expired.';
          console.error(err);
          this.isSubmitting = false;
        }
      });
    }
  }
}
