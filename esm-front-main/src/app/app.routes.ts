import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { Content } from './content/content';
import { Signin } from './pages/signin/signin';
import { Signup } from './pages/signup/signup';
import { Backoffice } from './pages/backoffice/backoffice';
import { DashboardAdminComponent } from './pages/dashboard-admin/dashboard-admin.component';
import { CoursesComponent } from './courses/courses.component';
import { CourseDetailComponent } from './courses/course-detail/course-detail.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', component: Content },
      { path: 'courses', component: CoursesComponent },
      { path: 'courses/:id', component: CourseDetailComponent },
      { path: 'enrollments', loadChildren: () => import('./enrollments/enrollments.routes').then(m => m.ENROLLMENTS_ROUTES) },
      { path: 'signin', component: Signin },
      { path: 'signup', component: Signup },
      { path: 'forgot-password', component: Signin },
      { path: 'support', component: Signin },
      { path: 'backoffice', component: Backoffice },
    ]
  },
  { path: 'dashboard_admin', component: DashboardAdminComponent },
  { path: '**', redirectTo: '' }
];
