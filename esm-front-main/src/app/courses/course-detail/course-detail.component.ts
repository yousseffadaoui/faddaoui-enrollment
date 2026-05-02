import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CoursesService, Course, Category, Instructor, Module, Review } from '../../services/courses.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { EnrollmentsApiService } from '../../services/enrollments-api.service';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

type Tab = 'overview' | 'curriculum' | 'reviews' | 'instructor';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './course-detail.component.html'
})
export class CourseDetailComponent implements OnInit {
  course: Course | null = null;
  category: Category | null = null;
  instructor: Instructor | null = null;
  modules: Module[] = [];
  reviews: Review[] = [];
  enrollmentCount = 0;
  loading = true;
  enrolling = false;
  error: string | null = null;
  activeTab: Tab = 'overview';
  expandedModules: Set<number> = new Set();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private coursesService: CoursesService,
    private enrollmentService: EnrollmentService,
    private enrollmentsApi: EnrollmentsApiService,
    private auth: AuthService,
    private toast: ToastService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadCourse(Number(id));
    }
  }

  loadCourse(id: number): void {
    this.loading = true;
    this.error = null;
    this.coursesService.getCourse(id).subscribe({
      next: (c) => {
        this.course = c;
        if (c.categoryId) {
          this.coursesService.getCategory(c.categoryId).subscribe({
            next: (cat) => { this.category = cat; this.cdr.markForCheck(); }
          });
        }
        if (c.instructorId) {
          this.coursesService.getInstructor(c.instructorId).subscribe({
            next: (inst) => { this.instructor = inst; this.cdr.markForCheck(); }
          });
        }
        this.coursesService.getModules(id, true).subscribe({
          next: (m) => { this.modules = m || []; this.cdr.markForCheck(); }
        });
        this.coursesService.getReviews(id, 0, 20).subscribe({
          next: (r) => { this.reviews = r.content || []; this.cdr.markForCheck(); }
        });
        this.enrollmentService.getEnrollmentCountByCourse().subscribe({
          next: (map) => {
            this.enrollmentCount = map.get(id) ?? 0;
            this.cdr.markForCheck();
          }
        });
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.message || 'Course not found';
        this.cdr.markForCheck();
      }
    });
  }

  setTab(t: Tab): void {
    this.activeTab = t;
  }

  toggleModule(id: number): void {
    if (this.expandedModules.has(id)) {
      this.expandedModules.delete(id);
    } else {
      this.expandedModules.add(id);
    }
    this.expandedModules = new Set(this.expandedModules);
    this.cdr.markForCheck();
  }

  isExpanded(id: number): boolean {
    return this.expandedModules.has(id);
  }

  getTotalLessons(): number {
    return this.modules.reduce((acc, m) => acc + (m.lessons?.length || 0), 0);
  }

  getTotalDuration(): number {
    return this.modules.reduce((acc, m) => {
      const sum = (m.lessons || []).reduce((a: number, l: any) => a + (l.durationMinutes || 0), 0);
      return acc + sum;
    }, 0);
  }

  stars(rating: number): number[] {
    return Array(5).fill(0).map((_, i) => i < Math.round(rating) ? 1 : 0);
  }

  private getEnrollErrorMessage(err: { status?: number; error?: { message?: string }; message?: string }): string {
    const msg = err?.error?.message ?? err?.message;
    if (msg) return msg;
    if (err?.status === 0 || err?.message?.includes('Unknown Error'))
      return 'Enrollment service unreachable. Start enrollment-service on port 8084 and try again.';
    return 'Failed to enroll.';
  }

  enrollInCourse(): void {
    const user = this.auth.user();
    if (!user?.id) {
      this.router.navigate(['/signin'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    const courseId = this.course?.courseId;
    if (courseId == null) return;
    this.enrolling = true;
    this.error = null;
    this.cdr.markForCheck();

    const idempotencyKey = `enroll-${user.id}-${courseId}`;
    this.enrollmentsApi
      .enroll(
        { courseId, userId: Number(user.id), studentName: user.name },
        idempotencyKey
      )
      .subscribe({
        next: () => {
          this.enrolling = false;
          this.toast.success('Successfully enrolled!');
          this.cdr.markForCheck();
          this.router.navigate(['/enrollments']);
        },
        error: err => {
          this.enrolling = false;
          this.error = this.getEnrollErrorMessage(err);
          this.toast.error(this.error ?? 'Failed to enroll.');
          this.cdr.markForCheck();
        }
      });
  }
}
