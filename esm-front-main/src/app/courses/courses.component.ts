import { Component, OnInit, ChangeDetectorRef, AfterViewInit, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CoursesService, Course, Category, Instructor } from '../services/courses.service';
import { normalizeThumbnailUrl } from '../utils/thumbnail';
import { EnrollmentService } from '../services/enrollment.service';

type SortOption = 'newest' | 'popular' | 'rating' | 'price-asc' | 'price-desc';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './courses.component.html'
})
export class CoursesComponent implements OnInit, AfterViewInit {
  private platformId = inject(PLATFORM_ID);

  courses: Course[] = [];
  categories: Category[] = [];
  instructors: Instructor[] = [];
  enrollmentCountByCourse: Map<number, number> = new Map();
  loading = true;
  error: string | null = null;
  searchQuery = '';
  filterLevel = '';
  filterCategoryId: number | null = null;
  filterPrice: '' | 'free' | 'paid' = '';
  filterMinRating = '';
  sortBy: SortOption = 'newest';
  currentPage = 0;
  totalPages = 1;
  totalElements = 0;
  pageSize = 12;

  constructor(
    private coursesService: CoursesService,
    private enrollmentService: EnrollmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadInstructors();
    this.loadEnrollmentCounts();
    this.loadCourses();
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      import('gsap').then(({ gsap }) => {
        const elements = document.querySelectorAll('.course-card');
        if (!elements.length) return;
        gsap.from(elements, { opacity: 0, y: 30, duration: 0.5, stagger: 0.08 });
      });
    }
  }

  loadCourses(): void {
    this.loading = true;
    this.error = null;
    const [sortBy, sortDir] = this.getSortParams();
    const params: any = {
      search: this.searchQuery?.trim() || undefined,
      level: this.filterLevel || undefined,
      categoryId: this.filterCategoryId ?? undefined,
      minRating: this.filterMinRating ? Number(this.filterMinRating) : undefined,
      freeOnly: this.filterPrice === 'free' ? true : this.filterPrice === 'paid' ? false : undefined,
      sortBy,
      sortDir,
      page: this.currentPage,
      size: this.pageSize
    };
    this.coursesService.searchCourses(params).subscribe({
      next: (data) => {
        this.courses = data.content || [];
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Error loading courses: ' + (err?.message || 'Unknown error');
        this.cdr.markForCheck();
      }
    });
  }

  private getSortParams(): [string, string] {
    switch (this.sortBy) {
      case 'newest': return ['courseId', 'desc'];
      case 'popular': return ['ratingCount', 'desc'];
      case 'rating': return ['ratingAvg', 'desc'];
      case 'price-asc': return ['price', 'asc'];
      case 'price-desc': return ['price', 'desc'];
      default: return ['courseId', 'desc'];
    }
  }

  loadCategories(): void {
    this.coursesService.getCategories().subscribe({
      next: (data) => {
        this.categories = Array.isArray(data) ? data : [];
        this.cdr.markForCheck();
      }
    });
  }

  loadInstructors(): void {
    this.coursesService.getInstructors().subscribe({
      next: (data) => {
        this.instructors = Array.isArray(data) ? data : [];
        this.cdr.markForCheck();
      }
    });
  }

  loadEnrollmentCounts(): void {
    this.enrollmentService.getEnrollmentCountByCourse().subscribe({
      next: (map) => {
        this.enrollmentCountByCourse = map;
        this.cdr.markForCheck();
      }
    });
  }

  getInstructorName(id: number | undefined): string {
    if (!id) return 'Instructor';
    const i = this.instructors.find(x => x.id === id);
    return i ? `${i.firstName} ${i.lastName}` : 'Instructor';
  }

  getEnrolledCount(courseId: number | undefined): number {
    if (!courseId) return 0;
    return this.enrollmentCountByCourse.get(courseId) ?? 0;
  }

  truncate(text: string | undefined, max: number): string {
    if (!text) return '';
    return text.length <= max ? text : text.slice(0, max) + '...';
  }

  /** Thumbnail URL - supports both camelCase and snake_case; normalizes for proxy */
  getThumbnail(course: Course): string | undefined {
    const c = course as Course & { thumbnail_url?: string };
    return normalizeThumbnailUrl(c.thumbnailUrl || c.thumbnail_url);
  }

  search(): void {
    this.currentPage = 0;
    this.loadCourses();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.filterLevel = '';
    this.filterCategoryId = null;
    this.filterPrice = '';
    this.filterMinRating = '';
    this.sortBy = 'newest';
    this.currentPage = 0;
    this.loadCourses();
  }

  goToPage(p: number): void {
    if (p >= 0 && p < this.totalPages) {
      this.currentPage = p;
      this.loadCourses();
    }
  }
}
