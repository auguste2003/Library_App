import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-too-many-requests',
    standalone: true,
    template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100">
      <div class="max-w-md w-full bg-white p-8 rounded-lg shadow-lg text-center">
        <div class="text-6xl mb-4">⚠️</div>
        <h1 class="text-3xl font-bold text-gray-800 mb-4">Too Many Requests</h1>
        <p class="text-gray-600 mb-8">
          Whoa there! You're making too many requests. Please slow down and try again in a moment.
        </p>
        <button 
          (click)="goBack()" 
          class="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-6 rounded transition duration-200">
          Go Back Home
        </button>
      </div>
    </div>
  `
})
export class TooManyRequestsComponent {
    constructor(private router: Router) { }

    goBack() {
        this.router.navigate(['/']);
    }
}
