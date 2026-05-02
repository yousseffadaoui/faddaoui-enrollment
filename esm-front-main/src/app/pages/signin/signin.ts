import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-signin',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './signin.html',
  styleUrl: './signin.css'
})
export class Signin {
  /** Default demo credentials for My Courses access */
  email = 'student';
  password = 'student123';
  loading = false;
  error: string | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  onSubmit(): void {
    this.error = null;
    if (!this.email?.trim()) {
      this.error = 'Please enter your email or username.';
      return;
    }
    if (!this.password) {
      this.error = 'Please enter your password.';
      return;
    }
    this.loading = true;
    this.auth.login(this.email.trim(), this.password).subscribe({
      next: () => {
        this.loading = false;
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: err => {
        this.loading = false;
        this.error = err?.message || 'Sign in failed. Please try again.';
      }
    });
  }
}
