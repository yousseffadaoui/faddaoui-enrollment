import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { filter, Subscription, forkJoin } from 'rxjs';
import { EnrollmentsApiService, MyCourse } from '../../../services/enrollments-api.service';
import { AuthService } from '../../../services/auth.service';
import { CoursesService } from '../../../services/courses.service';
import { ToastService } from '../../../services/toast.service';
import { EnrollmentCardComponent } from '../../components/enrollment-card/enrollment-card.component';
import { CancelConfirmModalComponent } from '../../components/cancel-confirm-modal/cancel-confirm-modal.component';

@Component({
  selector: 'app-my-courses',
  standalone: true,
  imports: [CommonModule, RouterLink, EnrollmentCardComponent, CancelConfirmModalComponent],
  templateUrl: './my-courses.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MyCoursesComponent implements OnInit, OnDestroy {
  private api = inject(EnrollmentsApiService);
  private auth = inject(AuthService);
  private coursesSvc = inject(CoursesService);
  private toast = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);
  private navSub?: Subscription;

  courses: MyCourse[] = [];
  continueCourses: MyCourse[] = [];
  instructorMap = new Map<number, string>();
  loading = true;
  error: string | null = null;
  viewMode: 'grid' | 'list' = 'grid';
  showCancelModal = false;
  cancelTargetId: number | null = null;

  get user() {
    return this.auth.user();
  }

  get userName() {
    return this.user?.name || 'Student';
  }

  ngOnInit(): void {
    this.load();
    this.navSub = this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      filter(() => this.router.url.includes('enrollments'))
    ).subscribe(() => this.load());
  }

  ngOnDestroy(): void {
    this.navSub?.unsubscribe();
  }

  load(): void {
    const user = this.auth.user();
    if (!user?.id) {
      this.loading = false;
      this.error = 'Please sign in to view your courses.';
      this.cdr.markForCheck();
      return;
    }
    const userId = Number(user.id);
    if (isNaN(userId) || userId < 1) {
      this.loading = false;
      this.error = 'Invalid user session. Please sign in again.';
      this.cdr.markForCheck();
      return;
    }
    this.loading = true;
    this.error = null;

    forkJoin({
      instructors: this.coursesSvc.getInstructors(),
      courses: this.coursesSvc.getCourses(),
      enrollments: this.api.getEnrollmentsByUser(userId)
    }).subscribe({
      next: ({ instructors, courses, enrollments }) => {
        this.instructorMap.clear();
        (instructors || []).forEach(i => {
          const name = [i.firstName, i.lastName].filter(Boolean).join(' ');
          if (i.id) this.instructorMap.set(i.id, name || 'Instructor');
        });

        const byCourse = new Map<number, any>();
        (enrollments || []).forEach(e => {
          if (e.courseId != null) {
            byCourse.set(e.courseId, e);
          }
        });

        const list: MyCourse[] = (courses || []).map(c => {
          const cid = c.courseId ?? (c as { id?: number }).id ?? 0;
          const e = cid ? byCourse.get(cid) : undefined;
          const hasEnrollment = !!e && (e.id ?? 0) !== 0;
          const status: string = hasEnrollment ? (e.status?.toString().toLowerCase() || 'active') : 'not_enrolled';
          return {
            enrollmentId: e?.id ?? 0,
            courseId: cid,
            status,
            progressPercent: e?.progressPercent ?? 0,
            enrolledAt: e?.enrolledAt,
            completedAt: e?.completedAt,
            course: {
              courseId: cid,
              name: c.name,
              level: c.level,
              thumbnailUrl: c.thumbnailUrl,
              instructorId: c.instructorId
            }
          };
        });

        this.applyCourses(list);
      },
      error: err => {
        this.loading = false;
        this.error = err?.error?.message || 'Failed to load courses.';
        this.cdr.markForCheck();
      }
    });
  }

  private applyCourses(list: MyCourse[]): void {
    this.courses = list;
    this.continueCourses = this.courses
      .filter(c => (c.status === 'active' || !c.status || c.status === 'in_progress') && (c.progressPercent ?? 0) < 100)
      .sort((a, b) => (b.progressPercent ?? 0) - (a.progressPercent ?? 0))
      .slice(0, 3);
    this.loading = false;
    this.cdr.markForCheck();
  }

  openCancelModal(id: number): void {
    this.cancelTargetId = id;
    this.showCancelModal = true;
    this.cdr.markForCheck();
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelTargetId = null;
    this.cdr.markForCheck();
  }

  confirmCancel(): void {
    if (!this.cancelTargetId) return;
    this.api.cancelEnrollment(this.cancelTargetId).subscribe({
      next: () => {
        this.toast.success('Enrollment cancelled.');
        this.closeCancelModal();
        this.load();
      },
      error: err => {
        this.toast.error(err?.error?.message || 'Failed to cancel.');
      }
    });
  }

  onCancelClick(id: number): void {
    this.openCancelModal(id);
  }

  onDeleteClick(id: number): void {
    if (!confirm('Remove this completed course from your list?')) return;
    this.api.deleteEnrollment(id).subscribe({
      next: () => {
        this.toast.success('Course removed.');
        this.load();
      },
      error: err => {
        this.toast.error(err?.error?.message || 'Failed to remove.');
      }
    });
  }

  onEnrollCourse(courseId: number): void {
    const user = this.auth.user();
    if (!user?.id) {
      this.toast.error('Please sign in to enroll.');
      return;
    }
    const userId = Number(user.id);
    const key = `enroll-${userId}-${courseId}`;
    this.api.enroll({ courseId, userId, studentName: user.name }, key).subscribe({
      next: () => {
        this.toast.success('Successfully enrolled!');
        // Refresh list and navigate using fresh data (avoids stale idempotency cache IDs)
        this.load();
        this.api.getEnrollmentsByUser(userId).subscribe({
          next: list => {
            const enr = (list || []).find(e => (e.courseId ?? 0) === courseId);
            if (enr?.id) {
              this.router.navigate(['/enrollments', 'progress', enr.id]);
            }
            this.cdr.markForCheck();
          },
          error: () => this.cdr.markForCheck()
        });
      },
      error: err => {
        const msg = err?.error?.message ?? err?.message
          ?? (err?.status === 0 ? 'Enrollment service unreachable. Start enrollment-service on port 8084.' : 'Failed to enroll.');
        this.toast.error(msg);
      }
    });
  }

  getInstructorName(course: MyCourse): string {
    const id = course.course?.instructorId;
    return id ? (this.instructorMap.get(id) ?? '') : '';
  }

  /** Unique track key to avoid NG0955 duplicate keys; uses courseId when valid, else index */
  trackByCourse(course: MyCourse, index: number): string | number {
    const id = course.courseId ?? course.course?.courseId;
    return (id != null && id !== 0) ? id : `idx-${index}`;
  }
}
