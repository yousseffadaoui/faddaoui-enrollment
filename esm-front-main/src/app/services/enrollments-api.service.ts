import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// Proxy rewrites /enrollment-api -> /api, so use /v1 to get /api/v1 on backend
const BASE = environment.enrollmentApiUrl
  ? `${environment.enrollmentApiUrl}/v1`
  : '/enrollment-api/v1';

export interface MyCourse {
  enrollmentId: number;
  courseId: number;
  status: string;
  progressPercent: number;
  enrolledAt?: string;
  completedAt?: string;
  course?: {
    courseId: number;
    name: string;
    level?: string;
    thumbnailUrl?: string;
    instructorId?: number;
  };
}

export interface Enrollment {
  id?: number;
  userId?: number;
  studentName?: string;
  courseId: number;
  status?: string;
  enrolledAt?: string;
  completedAt?: string;
  progressPercent?: number;
}

export interface LessonProgress {
  id: number;
  enrollmentId: number;
  lessonId: number;
  completedAt: string;
}

export interface ModuleWithLessons {
  id: number;
  courseId: number;
  title: string;
  orderIndex?: number;
  lessons?: Array<{
    id: number;
    moduleId: number;
    title: string;
    contentType?: string;
    durationMinutes?: number;
    orderIndex?: number;
  }>;
}

@Injectable({ providedIn: 'root' })
export class EnrollmentsApiService {
  constructor(private http: HttpClient) {}

  getMyCourses(userId: number): Observable<MyCourse[]> {
    return this.http.get<MyCourse[]>(`${BASE}/enrollments/user/${userId}/my-courses`);
  }

  getEnrollmentHistory(userId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${BASE}/enrollments/user/${userId}/history`);
  }

  getEnrollmentsByUser(userId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${BASE}/enrollments/user/${userId}`);
  }

  getEnrollment(id: number): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${BASE}/enrollments/${id}`);
  }

  cancelEnrollment(id: number): Observable<Enrollment> {
    return this.http.patch<Enrollment>(`${BASE}/enrollments/${id}/cancel`, {});
  }

  deleteEnrollment(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/enrollments/${id}`);
  }

  enroll(dto: { courseId: number; userId?: number; studentName?: string }, idempotencyKey?: string): Observable<Enrollment> {
    const headers = idempotencyKey
      ? new HttpHeaders({ 'X-Idempotency-Key': idempotencyKey })
      : undefined;
    return this.http.post<Enrollment>(`${BASE}/enrollments`, dto, headers ? { headers } : {});
  }

  getProgressPercent(enrollmentId: number): Observable<{ progressPercent: number }> {
    return this.http.get<{ progressPercent: number }>(`${BASE}/progress/enrollments/${enrollmentId}/percent`);
  }

  getCompletedLessons(enrollmentId: number): Observable<LessonProgress[]> {
    return this.http.get<LessonProgress[]>(`${BASE}/progress/enrollments/${enrollmentId}/lessons`);
  }

  markLessonComplete(enrollmentId: number, lessonId: number): Observable<LessonProgress> {
    return this.http.post<LessonProgress>(
      `${BASE}/progress/enrollments/${enrollmentId}/lessons/${lessonId}/complete`,
      {}
    );
  }

  downloadCertificate(enrollmentId: number): Observable<Blob> {
    return this.http.get(`${BASE}/certificates/enrollment/${enrollmentId}/download`, {
      responseType: 'blob'
    });
  }
}
