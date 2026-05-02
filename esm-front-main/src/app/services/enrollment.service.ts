import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

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

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  // Proxy rewrites /enrollment-api -> /api, so use /v1 to get /api/v1 on backend
  private readonly apiUrl = environment.enrollmentApiUrl
    ? `${environment.enrollmentApiUrl}/v1/enrollments`
    : '/enrollment-api/v1/enrollments';

  constructor(private http: HttpClient) {}

  getEnrollments(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(this.apiUrl);
  }

  /** Count of enrollments per courseId (for display). Call getEnrollments and aggregate client-side. */
  getEnrollmentCountByCourse(): Observable<Map<number, number>> {
    return this.http.get<Enrollment[]>(this.apiUrl).pipe(
      map(list => {
        const map = new Map<number, number>();
        (list || []).forEach(e => {
          const cid = e.courseId;
          map.set(cid, (map.get(cid) || 0) + 1);
        });
        return map;
      })
    );
  }

  getEnrollment(id: number): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.apiUrl}/${id}`);
  }

  createEnrollment(enrollment: Enrollment): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.apiUrl, enrollment);
  }

  getEnrollmentsByUser(userId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/user/${userId}`);
  }

  cancelEnrollment(id: number): Observable<Enrollment> {
    return this.http.patch<Enrollment>(`${this.apiUrl}/${id}/cancel`, {});
  }

  updateEnrollment(id: number, enrollment: Enrollment): Observable<Enrollment> {
    return this.http.put<Enrollment>(`${this.apiUrl}/${id}`, enrollment);
  }

  deleteEnrollment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
