import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SetupService } from '../../../core/services/setup.service';

@Component({
  selector: 'app-setup',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './setup.html',
  styleUrl: './setup.css'
})
export class SetupComponent {
  private fb = inject(FormBuilder);
  private setupService = inject(SetupService);
  private router = inject(Router);

  setupForm: FormGroup = this.fb.group({
    firstname: ['', Validators.required],
    lastname: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/)
    ]]
  });

  errorMessage = '';
  isSubmitting = false;

  onSubmit(): void {
    if (this.setupForm.valid) {
      this.isSubmitting = true;
      this.setupService.createInitialAdmin(this.setupForm.value).subscribe({
        next: () => {
          // Admin created, redirect to login
          this.router.navigate(['/login']);
        },
        error: (err) => {
          this.errorMessage = err.error?.error || 'Failed to create admin. Setup might already be completed.';
          this.isSubmitting = false;
        }
      });
    }
  }
}
