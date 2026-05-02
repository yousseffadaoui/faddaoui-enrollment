import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EnrollmentsApiService, Enrollment } from '../../../services/enrollments-api.service';
import { AuthService } from '../../../services/auth.service';
@Component({
  selector: 'app-enrollment-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './enrollment-history.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnrollmentHistoryComponent implements OnInit {
  private api = inject(EnrollmentsApiService);
  private auth = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  enrollments: Enrollment[] = [];
  filtered: Enrollment[] = [];
  loading = true;
  error: string | null = null;
  filter: 'all' | 'completed' | 'active' | 'cancelled' = 'all';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const user = this.auth.user();
    if (!user?.id) {
      this.loading = false;
      this.error = 'Please sign in.';
      this.cdr.markForCheck();
      return;
    }
    this.api.getEnrollmentHistory(Number(user.id)).subscribe({
      next: data => {
        this.enrollments = data || [];
        this.applyFilter();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to load history.';
        this.cdr.markForCheck();
      }
    });
  }

  setFilter(f: 'all' | 'completed' | 'active' | 'cancelled'): void {
    this.filter = f;
    this.applyFilter();
    this.cdr.markForCheck();
  }

  private applyFilter(): void {
    if (this.filter === 'all') {
      this.filtered = [...this.enrollments];
    } else {
      this.filtered = this.enrollments.filter(e => (e.status || '').toLowerCase() === this.filter);
    }
  }

  statusLabel(s: string | undefined): string {
    const v = (s || '').toLowerCase();
    if (v === 'completed') return 'Completed';
    if (v === 'cancelled') return 'Cancelled';
    return 'In Progress';
  }

  statusClass(s: string | undefined): string {
    const v = (s || '').toLowerCase();
    if (v === 'completed') return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400';
    if (v === 'cancelled') return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400';
    return 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-400';
  }
}
