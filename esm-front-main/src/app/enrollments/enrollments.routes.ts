import { Routes } from '@angular/router';
import { authGuard } from '../guards/auth.guard';
import { roleGuard } from '../guards/role.guard';

export const ENROLLMENTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/my-courses/my-courses.component').then(m => m.MyCoursesComponent),
    canActivate: [authGuard, roleGuard(['STUDENT', 'ADMIN'])]
  },
  {
    path: 'progress/:enrollmentId',
    loadComponent: () => import('./pages/course-progress/course-progress.component').then(m => m.CourseProgressComponent),
    canActivate: [authGuard, roleGuard(['STUDENT', 'ADMIN'])]
  },
  {
    path: 'history',
    loadComponent: () => import('./pages/enrollment-history/enrollment-history.component').then(m => m.EnrollmentHistoryComponent),
    canActivate: [authGuard, roleGuard(['STUDENT', 'ADMIN'])]
  },
  {
    path: 'certificate/:enrollmentId',
    loadComponent: () => import('./pages/certificate-view/certificate-view.component').then(m => m.CertificateViewComponent),
    canActivate: [authGuard, roleGuard(['STUDENT', 'ADMIN'])]
  }
];
