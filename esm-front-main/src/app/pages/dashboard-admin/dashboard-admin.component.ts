import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CoursesService,
  Course,
  Category,
  Instructor,
  Module,
  Lesson,
  Review,
  PageResponse
} from '../../services/courses.service';
import { EnrollmentService, Enrollment } from '../../services/enrollment.service';
import { normalizeThumbnailUrl } from '../../utils/thumbnail';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard-admin.component.html',
  styleUrls: ['./dashboard-admin.component.css'],
})
export class DashboardAdminComponent implements OnInit {
  activeTab: 'courses' | 'enrollments' | 'categories' | 'instructors' = 'courses';

  // Courses
  courses: Course[] = [];
  coursesPage: PageResponse<Course> | null = null;
  coursesLoading = false;
  coursesError: string | null = null;
  courseForm: Partial<Course> = { name: '', level: 'A1', description: '' };
  editingCourseId: number | null = null;
  showCourseForm = false;
  courseFormUploading = false;
  searchQuery = '';
  filterLevel = '';
  filterCategoryId: number | null = null;
  filterInstructorId: number | null = null;
  coursePage = 0;
  coursePageSize = 10;
  useSearch = false;

  // Categories
  categories: Category[] = [];
  categoriesLoading = false;
  categoriesError: string | null = null;
  categoryForm: Partial<Category> = { name: '', description: '' };
  editingCategoryId: number | null = null;
  showCategoryForm = false;

  // Instructors
  instructors: Instructor[] = [];
  instructorsLoading = false;
  instructorsError: string | null = null;
  instructorForm: Partial<Instructor> = { firstName: '', lastName: '', email: '' };
  editingInstructorId: number | null = null;
  showInstructorForm = false;

  // Course detail (modules/lessons)
  selectedCourseId: number | null = null;
  courseModules: Module[] = [];
  modulesLoading = false;
  courseDetailError: string | null = null;
  showModuleForm = false;
  showLessonForm = false;
  moduleForm: Partial<Module> = { title: '' };
  moduleFormErrors: { title?: string } = {};
  lessonForm: Partial<Lesson> = { title: '', contentType: 'VIDEO', contentUrl: '', contentText: '', durationMinutes: 0 };
  editingModuleId: number | null = null;
  editingLessonId: number | null = null;
  selectedModuleId: number | null = null;

  // Enrollments
  enrollments: Enrollment[] = [];
  enrollmentsLoading = false;
  enrollmentsError: string | null = null;
  enrollmentForm: Partial<Enrollment> = { studentName: '', courseId: 0, status: 'active' };
  editingEnrollmentId: number | null = null;
  showEnrollmentForm = false;

  constructor(
    private coursesService: CoursesService,
    private enrollmentService: EnrollmentService
  ) {}

  ngOnInit(): void {
    this.loadCourses();
    this.loadEnrollments();
    this.loadCategories();
    this.loadInstructors();
  }

  setTab(tab: 'courses' | 'enrollments' | 'categories' | 'instructors'): void {
    this.activeTab = tab;
  }

  get displayedCourses(): Course[] {
    if (this.useSearch && this.coursesPage) return this.coursesPage.content;
    return this.courses;
  }

  get totalCoursePages(): number {
    return this.coursesPage?.totalPages ?? 0;
  }

  get hasSearchFilters(): boolean {
    return !!(
      this.searchQuery?.trim() ||
      this.filterLevel ||
      this.filterCategoryId != null ||
      this.filterInstructorId != null
    );
  }

  // ========== COURSES ==========
  loadCourses(): void {
    this.coursesLoading = true;
    this.coursesError = null;
    if (this.hasSearchFilters || this.useSearch) {
      this.coursesService
        .searchCourses({
          search: this.searchQuery?.trim() || undefined,
          level: this.filterLevel || undefined,
          categoryId: this.filterCategoryId ?? undefined,
          instructorId: this.filterInstructorId ?? undefined,
          page: this.coursePage,
          size: this.coursePageSize
        })
        .subscribe({
          next: (data) => {
            this.coursesPage = data;
            this.courses = [];
            this.coursesLoading = false;
          },
          error: (err) => {
            this.coursesError = err?.message || 'Failed to load courses';
            this.coursesLoading = false;
          }
        });
    } else {
      this.coursesService.getCourses().subscribe({
        next: (data) => {
          this.courses = Array.isArray(data) ? data : [];
          this.coursesPage = null;
          this.coursesLoading = false;
        },
        error: (err) => {
          this.coursesError = err?.message || 'Failed to load courses';
          this.coursesLoading = false;
        }
      });
    }
  }

  applySearch(): void {
    this.useSearch = true;
    this.coursePage = 0;
    this.loadCourses();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.filterLevel = '';
    this.filterCategoryId = null;
    this.filterInstructorId = null;
    this.useSearch = false;
    this.coursePage = 0;
    this.loadCourses();
  }

  goToCoursePage(page: number): void {
    this.coursePage = page;
    this.loadCourses();
  }

  openAddCourse(): void {
    this.editingCourseId = null;
    this.courseForm = {
      name: '',
      level: 'A1',
      description: '',
      categoryId: undefined,
      instructorId: undefined,
      price: 0,
      thumbnailUrl: ''
    };
    this.showCourseForm = true;
  }

  openEditCourse(course: Course): void {
    this.editingCourseId = course.courseId ?? null;
    this.courseForm = { ...course };
    this.showCourseForm = true;
  }

  cancelCourseForm(): void {
    this.showCourseForm = false;
    this.editingCourseId = null;
  }

  getThumbnailPreview(): string | undefined {
    return normalizeThumbnailUrl(this.courseForm.thumbnailUrl);
  }

  onThumbnailFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !file.type.startsWith('image/')) {
      this.coursesError = 'Please select a valid image (JPEG, PNG, GIF, WebP).';
      return;
    }
    if (file.size > 200 * 1024 * 1024) {
      this.coursesError = 'Image must be under 200 MB.';
      return;
    }
    this.courseFormUploading = true;
    this.coursesError = null;
    this.coursesService.uploadImage(file).subscribe({
      next: (res) => {
        this.courseForm.thumbnailUrl = res.url;
        this.courseFormUploading = false;
        input.value = '';
      },
      error: (err) => {
        const msg = err?.error?.error || err?.error?.message || err?.message;
        this.coursesError = msg || `Upload failed (${err?.status || 'network error'})`;
        this.courseFormUploading = false;
      }
    });
  }

  saveCourse(): void {
    const name = (this.courseForm.name || '').trim();
    const level = (this.courseForm.level || '').trim();
    const description = (this.courseForm.description || '').trim();
    if (!name || !level) {
      this.coursesError = 'Name and Level are required.';
      return;
    }
    const price = this.courseForm.price ?? 0;
    if (price < 0) {
      this.coursesError = 'Price cannot be negative.';
      return;
    }

    this.coursesError = null;
    const payload: Course = {
      name,
      level,
      description: description || undefined,
      categoryId: this.courseForm.categoryId ?? undefined,
      instructorId: this.courseForm.instructorId ?? undefined,
      price,
      thumbnailUrl: this.courseForm.thumbnailUrl
    };

    if (this.editingCourseId) {
      this.coursesService.updateCourse(this.editingCourseId, payload).subscribe({
        next: () => {
          this.loadCourses();
          this.loadCategories();
          this.loadInstructors();
          this.cancelCourseForm();
        },
        error: (err) => {
          this.coursesError = err?.error?.error || err?.error?.message || err?.message || 'Failed to update course';
        }
      });
    } else {
      this.coursesService.createCourse(payload).subscribe({
        next: () => {
          this.loadCourses();
          this.loadCategories();
          this.loadInstructors();
          this.cancelCourseForm();
        },
        error: (err) => {
          this.coursesError = err?.error?.error || err?.error?.message || err?.message || 'Failed to create course';
        }
      });
    }
  }

  deleteCourse(course: Course): void {
    const id = course.courseId;
    if (!id || !confirm(`Delete course "${course.name}"?`)) return;
    this.coursesService.deleteCourse(id).subscribe({
      next: () => this.loadCourses(),
      error: (err) => {
        this.coursesError = err?.message || 'Failed to delete course';
      }
    });
  }

  openCourseDetail(courseId: number): void {
    this.selectedCourseId = courseId;
    this.loadCourseModules();
  }

  closeCourseDetail(): void {
    this.selectedCourseId = null;
    this.courseModules = [];
    this.courseDetailError = null;
    this.showModuleForm = false;
    this.showLessonForm = false;
  }

  loadCourseModules(): void {
    if (!this.selectedCourseId) return;
    this.modulesLoading = true;
    this.courseDetailError = null;
    this.coursesService.getModules(this.selectedCourseId, true).subscribe({
      next: (data) => {
        this.courseModules = data || [];
        this.modulesLoading = false;
      },
      error: (err) => {
        this.courseDetailError = err?.error?.message || err?.message || 'Failed to load modules';
        this.modulesLoading = false;
      }
    });
  }

  openAddModule(): void {
    if (!this.selectedCourseId) return;
    this.editingModuleId = null;
    this.moduleForm = { courseId: this.selectedCourseId, title: '' };
    this.moduleFormErrors = {};
    this.showModuleForm = true;
    this.courseDetailError = null;
  }

  openEditModule(m: Module): void {
    this.editingModuleId = m.id ?? null;
    this.moduleForm = { courseId: this.selectedCourseId!, title: m.title, orderIndex: m.orderIndex };
    this.moduleFormErrors = {};
    this.showModuleForm = true;
    this.courseDetailError = null;
  }

  cancelModuleForm(): void {
    this.showModuleForm = false;
    this.editingModuleId = null;
    this.moduleFormErrors = {};
  }

  validateModuleForm(): boolean {
    this.moduleFormErrors = {};
    const title = (this.moduleForm.title || '').trim();
    if (!title) {
      this.moduleFormErrors['title'] = 'Le titre du module est requis.';
      return false;
    }
    if (title.length < 2) {
      this.moduleFormErrors['title'] = 'Le titre doit contenir au moins 2 caractères.';
      return false;
    }
    if (title.length > 100) {
      this.moduleFormErrors['title'] = 'Le titre ne doit pas dépasser 100 caractères.';
      return false;
    }
    return true;
  }

  get moduleTitleLength(): number {
    return (this.moduleForm.title || '').length;
  }

  saveModule(): void {
    if (!this.moduleForm.courseId) return;
    if (!this.validateModuleForm()) return;
    const title = (this.moduleForm.title || '').trim();
    this.courseDetailError = null;
    const payload: Module = {
      courseId: this.moduleForm.courseId,
      title,
      orderIndex: this.moduleForm.orderIndex ?? 0
    };
    if (this.editingModuleId) {
      this.coursesService.updateModule(this.editingModuleId, payload).subscribe({
        next: () => {
          this.loadCourseModules();
          this.showModuleForm = false;
          this.editingModuleId = null;
        },
        error: (err) => {
          this.courseDetailError = err?.error?.message || err?.message || 'Failed to update module';
        }
      });
    } else {
      this.coursesService.createModule(payload).subscribe({
        next: () => {
          this.loadCourseModules();
          this.showModuleForm = false;
        },
        error: (err) => {
          this.courseDetailError = err?.error?.message || err?.message || 'Failed to create module';
        }
      });
    }
  }

  openAddLesson(moduleId: number): void {
    this.selectedModuleId = moduleId;
    this.editingLessonId = null;
    const mod = this.courseModules.find(m => m.id === moduleId);
    const nextOrder = (mod?.lessons?.length ?? 0);
    this.lessonForm = { moduleId, title: '', contentType: 'VIDEO', contentUrl: '', contentText: '', durationMinutes: 0, orderIndex: nextOrder };
    this.showLessonForm = true;
    this.courseDetailError = null;
  }

  openEditLesson(l: Lesson): void {
    this.selectedModuleId = l.moduleId;
    this.editingLessonId = l.id ?? null;
    this.lessonForm = {
      moduleId: l.moduleId,
      title: l.title,
      contentType: l.contentType || 'TEXT',
      contentUrl: l.contentUrl || '',
      contentText: l.contentText || '',
      durationMinutes: l.durationMinutes ?? 0,
      orderIndex: l.orderIndex ?? 0
    };
    this.showLessonForm = true;
    this.courseDetailError = null;
  }

  cancelLessonForm(): void {
    this.showLessonForm = false;
    this.editingLessonId = null;
    this.selectedModuleId = null;
  }

  saveLesson(): void {
    const title = (this.lessonForm.title || '').trim();
    if (!this.lessonForm.moduleId || !title) {
      this.courseDetailError = 'Lesson title is required.';
      return;
    }
    this.courseDetailError = null;
    const payload: Lesson = {
      moduleId: this.lessonForm.moduleId,
      title,
      contentType: this.lessonForm.contentType || 'TEXT',
      contentUrl: this.lessonForm.contentUrl?.trim() || undefined,
      contentText: this.lessonForm.contentText?.trim() || undefined,
      quizContentJson: this.lessonForm.quizContentJson?.trim() || undefined,
      durationMinutes: Math.max(0, this.lessonForm.durationMinutes ?? 0),
      orderIndex: Math.max(0, this.lessonForm.orderIndex ?? 0)
    };
    if (this.editingLessonId) {
      this.coursesService.updateLesson(this.editingLessonId, payload).subscribe({
        next: () => {
          this.loadCourseModules();
          this.showLessonForm = false;
          this.editingLessonId = null;
          this.selectedModuleId = null;
        },
        error: (err) => {
          this.courseDetailError = err?.error?.message || err?.message || 'Failed to update lesson';
        }
      });
    } else {
      this.coursesService.createLesson(payload).subscribe({
        next: () => {
          this.loadCourseModules();
          this.showLessonForm = false;
          this.selectedModuleId = null;
        },
        error: (err) => {
          this.courseDetailError = err?.error?.message || err?.message || 'Failed to create lesson';
        }
      });
    }
  }

  deleteModule(m: Module): void {
    if (!m.id || !confirm('Delete module?')) return;
    this.coursesService.deleteModule(m.id).subscribe({
      next: () => this.loadCourseModules()
    });
  }

  deleteLesson(l: Lesson, module: Module): void {
    if (!l.id || !confirm('Delete lesson?')) return;
    this.coursesService.deleteLesson(l.id).subscribe({
      next: () => this.loadCourseModules()
    });
  }

  // ========== CATEGORIES ==========
  loadCategories(): void {
    this.categoriesLoading = true;
    this.categoriesError = null;
    this.coursesService.getCategories().subscribe({
      next: (data) => {
        this.categories = Array.isArray(data) ? data : [];
        this.categoriesLoading = false;
      },
      error: (err) => {
        this.categoriesError = err?.message || 'Failed to load categories';
        this.categoriesLoading = false;
      }
    });
  }

  openAddCategory(): void {
    this.editingCategoryId = null;
    this.categoryForm = { name: '', description: '' };
    this.showCategoryForm = true;
  }

  openEditCategory(c: Category): void {
    this.editingCategoryId = c.id ?? null;
    this.categoryForm = { ...c };
    this.showCategoryForm = true;
  }

  cancelCategoryForm(): void {
    this.showCategoryForm = false;
    this.editingCategoryId = null;
  }

  saveCategory(): void {
    const name = (this.categoryForm.name || '').trim();
    if (!name) return;
    const payload: Category = { name, description: this.categoryForm.description, slug: this.categoryForm.slug };
    if (this.editingCategoryId) {
      this.coursesService.updateCategory(this.editingCategoryId, payload).subscribe({
        next: () => {
          this.loadCategories();
          this.loadCourses();
          this.cancelCategoryForm();
        },
        error: (err) => {
          this.categoriesError = err?.message || 'Failed to update category';
        }
      });
    } else {
      this.coursesService.createCategory(payload).subscribe({
        next: () => {
          this.loadCategories();
          this.loadCourses();
          this.cancelCategoryForm();
        },
        error: (err) => {
          this.categoriesError = err?.message || 'Failed to create category';
        }
      });
    }
  }

  deleteCategory(c: Category): void {
    if (!c.id || !confirm(`Delete category "${c.name}"?`)) return;
    this.coursesService.deleteCategory(c.id).subscribe({
      next: () => this.loadCategories(),
      error: (err) => {
        this.categoriesError = err?.message || 'Failed to delete category';
      }
    });
  }

  // ========== INSTRUCTORS ==========
  loadInstructors(): void {
    this.instructorsLoading = true;
    this.instructorsError = null;
    this.coursesService.getInstructors().subscribe({
      next: (data) => {
        this.instructors = Array.isArray(data) ? data : [];
        this.instructorsLoading = false;
      },
      error: (err) => {
        this.instructorsError = err?.message || 'Failed to load instructors';
        this.instructorsLoading = false;
      }
    });
  }

  openAddInstructor(): void {
    this.editingInstructorId = null;
    this.instructorForm = { firstName: '', lastName: '', email: '' };
    this.showInstructorForm = true;
  }

  openEditInstructor(i: Instructor): void {
    this.editingInstructorId = i.id ?? null;
    this.instructorForm = { ...i };
    this.showInstructorForm = true;
  }

  cancelInstructorForm(): void {
    this.showInstructorForm = false;
    this.editingInstructorId = null;
  }

  saveInstructor(): void {
    const firstName = (this.instructorForm.firstName || '').trim();
    const lastName = (this.instructorForm.lastName || '').trim();
    if (!firstName || !lastName) return;
    const payload: Instructor = {
      firstName,
      lastName,
      email: this.instructorForm.email,
      bio: this.instructorForm.bio,
      avatarUrl: this.instructorForm.avatarUrl
    };
    if (this.editingInstructorId) {
      this.coursesService.updateInstructor(this.editingInstructorId, payload).subscribe({
        next: () => {
          this.loadInstructors();
          this.loadCourses();
          this.cancelInstructorForm();
        },
        error: (err) => {
          this.instructorsError = err?.message || 'Failed to update instructor';
        }
      });
    } else {
      this.coursesService.createInstructor(payload).subscribe({
        next: () => {
          this.loadInstructors();
          this.loadCourses();
          this.cancelInstructorForm();
        },
        error: (err) => {
          this.instructorsError = err?.message || 'Failed to create instructor';
        }
      });
    }
  }

  deleteInstructor(i: Instructor): void {
    if (!i.id || !confirm(`Delete instructor "${i.firstName} ${i.lastName}"?`)) return;
    this.coursesService.deleteInstructor(i.id).subscribe({
      next: () => this.loadInstructors(),
      error: (err) => {
        this.instructorsError = err?.message || 'Failed to delete instructor';
      }
    });
  }

  getCategoryName(id: number | undefined): string {
    if (!id) return '-';
    const c = this.categories.find((x) => x.id === id);
    return c?.name ?? String(id);
  }

  getInstructorName(id: number | undefined): string {
    if (!id) return '-';
    const i = this.instructors.find((x) => x.id === id);
    return i ? `${i.firstName} ${i.lastName}` : String(id);
  }

  isLessonFormForModule(moduleId: number | undefined): boolean {
    return !!moduleId && this.selectedModuleId === moduleId && this.showLessonForm;
  }

  // ========== ENROLLMENTS ==========
  loadEnrollments(): void {
    this.enrollmentsLoading = true;
    this.enrollmentsError = null;
    this.enrollmentService.getEnrollments().subscribe({
      next: (data) => {
        this.enrollments = Array.isArray(data) ? data : [];
        this.enrollmentsLoading = false;
      },
      error: (err) => {
        this.enrollmentsError = err?.message || 'Failed to load enrollments';
        this.enrollmentsLoading = false;
      },
    });
  }

  openAddEnrollment(): void {
    this.editingEnrollmentId = null;
    this.enrollmentForm = { studentName: '', courseId: 0, status: 'active' };
    this.showEnrollmentForm = true;
  }

  openEditEnrollment(e: Enrollment): void {
    this.editingEnrollmentId = e.id ?? null;
    this.enrollmentForm = { ...e };
    this.showEnrollmentForm = true;
  }

  cancelEnrollmentForm(): void {
    this.showEnrollmentForm = false;
    this.editingEnrollmentId = null;
    this.enrollmentForm = { studentName: '', courseId: 0, status: 'active' };
  }

  saveEnrollment(): void {
    const studentName = (this.enrollmentForm.studentName || '').trim();
    const courseId = this.enrollmentForm.courseId ?? 0;
    const status = (this.enrollmentForm.status || 'active').trim();
    if (!studentName || !courseId) return;

    const payload: Enrollment = { studentName, courseId, status };

    if (this.editingEnrollmentId) {
      this.enrollmentService.updateEnrollment(this.editingEnrollmentId, payload).subscribe({
        next: () => {
          this.loadEnrollments();
          this.cancelEnrollmentForm();
        },
        error: (err) => {
          this.enrollmentsError = err?.message || 'Failed to update enrollment';
        },
      });
    } else {
      this.enrollmentService.createEnrollment(payload).subscribe({
        next: () => {
          this.loadEnrollments();
          this.cancelEnrollmentForm();
        },
        error: (err) => {
          this.enrollmentsError = err?.message || 'Failed to create enrollment';
        },
      });
    }
  }

  deleteEnrollment(e: Enrollment): void {
    const id = e.id;
    if (!id || !confirm(`Delete enrollment #${id}?`)) return;
    this.enrollmentService.deleteEnrollment(id).subscribe({
      next: () => this.loadEnrollments(),
      error: (err) => {
        this.enrollmentsError = err?.message || 'Failed to delete enrollment';
      },
    });
  }
}
