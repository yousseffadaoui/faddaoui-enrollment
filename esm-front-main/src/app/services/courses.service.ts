import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Course {
  courseId?: number;
  name: string;
  level: string;
  description?: string;
  categoryId?: number;
  instructorId?: number;
  price?: number;
  thumbnailUrl?: string;
  isPublished?: boolean;
  ratingAvg?: number;
  ratingCount?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Category {
  id?: number;
  name: string;
  description?: string;
  slug?: string;
}

export interface Instructor {
  id?: number;
  firstName: string;
  lastName: string;
  email?: string;
  bio?: string;
  avatarUrl?: string;
}

export interface Module {
  id?: number;
  courseId: number;
  title: string;
  orderIndex?: number;
  lessons?: Lesson[];
}

export interface Lesson {
  id?: number;
  moduleId: number;
  title: string;
  contentType?: 'VIDEO' | 'PDF' | 'QUIZ' | 'TEXT';
  contentUrl?: string;
  contentText?: string;
  quizContentJson?: string;
  durationMinutes?: number;
  orderIndex?: number;
}

export interface Review {
  id?: number;
  courseId: number;
  userId: number;
  rating: number;
  comment?: string;
  createdAt?: string;
}

export interface CourseSearchParams {
  search?: string;
  level?: string;
  categoryId?: number;
  instructorId?: number;
  isPublished?: boolean;
  minRating?: number;
  freeOnly?: boolean;  // true=free only, false=paid only
  sortBy?: string;     // courseId, ratingAvg, ratingCount, price, name
  sortDir?: string;    // asc, desc
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class CoursesService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ========== COURSES ==========
  getCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.baseUrl}/api/v1/courses`);
  }

  searchCourses(params: CourseSearchParams): Observable<PageResponse<Course>> {
    let httpParams = new HttpParams();
    if (params.search) httpParams = httpParams.set('search', params.search);
    if (params.level) httpParams = httpParams.set('level', params.level);
    if (params.categoryId != null) httpParams = httpParams.set('categoryId', params.categoryId);
    if (params.instructorId != null) httpParams = httpParams.set('instructorId', params.instructorId);
    if (params.isPublished != null) httpParams = httpParams.set('isPublished', params.isPublished);
    if (params.minRating != null) httpParams = httpParams.set('minRating', params.minRating);
    if (params.freeOnly != null) httpParams = httpParams.set('freeOnly', params.freeOnly);
    if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
    if (params.sortDir) httpParams = httpParams.set('sortDir', params.sortDir);
    if (params.page != null) httpParams = httpParams.set('page', params.page);
    if (params.size != null) httpParams = httpParams.set('size', params.size);
    return this.http.get<PageResponse<Course>>(`${this.baseUrl}/api/v1/courses`, { params: httpParams });
  }

  getCourse(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.baseUrl}/api/v1/courses/${id}`);
  }

  createCourse(course: Course): Observable<Course> {
    return this.http.post<Course>(`${this.baseUrl}/api/v1/courses`, course);
  }

  updateCourse(id: number, course: Course): Observable<Course> {
    return this.http.put<Course>(`${this.baseUrl}/api/v1/courses/${id}`, course);
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/courses/${id}`);
  }

  /** Upload course thumbnail image; returns { url: string } */
  uploadImage(file: File): Observable<{ url: string; filename?: string }> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string; filename?: string }>(`${this.baseUrl}/api/v1/upload/image`, formData);
  }

  publishCourse(id: number): Observable<Course> {
    return this.http.patch<Course>(`${this.baseUrl}/api/v1/courses/${id}/publish`, {});
  }

  // ========== CATEGORIES ==========
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/api/v1/categories`);
  }

  getCategory(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.baseUrl}/api/v1/categories/${id}`);
  }

  createCategory(cat: Category): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/api/v1/categories`, cat);
  }

  updateCategory(id: number, cat: Category): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/api/v1/categories/${id}`, cat);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/categories/${id}`);
  }

  // ========== INSTRUCTORS ==========
  getInstructors(): Observable<Instructor[]> {
    return this.http.get<Instructor[]>(`${this.baseUrl}/api/v1/instructors`);
  }

  getInstructor(id: number): Observable<Instructor> {
    return this.http.get<Instructor>(`${this.baseUrl}/api/v1/instructors/${id}`);
  }

  createInstructor(inst: Instructor): Observable<Instructor> {
    return this.http.post<Instructor>(`${this.baseUrl}/api/v1/instructors`, inst);
  }

  updateInstructor(id: number, inst: Instructor): Observable<Instructor> {
    return this.http.put<Instructor>(`${this.baseUrl}/api/v1/instructors/${id}`, inst);
  }

  deleteInstructor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/instructors/${id}`);
  }

  // ========== MODULES ==========
  getModules(courseId: number, includeLessons = false): Observable<Module[]> {
    let httpParams = new HttpParams().set('courseId', courseId.toString());
    if (includeLessons) {
      httpParams = httpParams.set('includeLessons', 'true');
    }
    return this.http.get<Module[]>(`${this.baseUrl}/api/v1/modules`, { params: httpParams });
  }

  getModule(id: number, includeLessons = false): Observable<Module> {
    const params = includeLessons ? new HttpParams().set('includeLessons', 'true') : undefined;
    return this.http.get<Module>(`${this.baseUrl}/api/v1/modules/${id}`, params ? { params } : {});
  }

  createModule(module: Module): Observable<Module> {
    return this.http.post<Module>(`${this.baseUrl}/api/v1/modules`, module);
  }

  updateModule(id: number, module: Module): Observable<Module> {
    return this.http.put<Module>(`${this.baseUrl}/api/v1/modules/${id}`, module);
  }

  deleteModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/modules/${id}`);
  }

  // ========== LESSONS ==========
  getLessons(moduleId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.baseUrl}/api/v1/lessons`, { params: { moduleId } });
  }

  getLesson(id: number): Observable<Lesson> {
    return this.http.get<Lesson>(`${this.baseUrl}/api/v1/lessons/${id}`);
  }

  createLesson(lesson: Lesson): Observable<Lesson> {
    return this.http.post<Lesson>(`${this.baseUrl}/api/v1/lessons`, lesson);
  }

  updateLesson(id: number, lesson: Lesson): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.baseUrl}/api/v1/lessons/${id}`, lesson);
  }

  deleteLesson(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/lessons/${id}`);
  }

  // ========== REVIEWS ==========
  getReviews(courseId: number, page = 0, size = 20): Observable<PageResponse<Review>> {
    return this.http.get<PageResponse<Review>>(`${this.baseUrl}/api/v1/reviews`, {
      params: { courseId, page: page.toString(), size: size.toString() }
    });
  }

  createReview(review: Review): Observable<Review> {
    return this.http.post<Review>(`${this.baseUrl}/api/v1/reviews`, review);
  }

  updateReview(id: number, review: Review): Observable<Review> {
    return this.http.put<Review>(`${this.baseUrl}/api/v1/reviews/${id}`, review);
  }

  deleteReview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/reviews/${id}`);
  }
}
