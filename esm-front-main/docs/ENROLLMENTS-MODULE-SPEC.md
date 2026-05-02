# Enrollments Module – Specification & Structure

## 1. Angular Folder Structure (Lazy-loaded EnrollmentsModule)

```
src/app/
├── enrollments/
│   ├── enrollments.routes.ts           # Lazy-loaded routes
│   ├── enrollments.component.ts        # Shell / layout
│   ├── enrollments.component.html
│   │
│   ├── pages/
│   │   ├── my-courses/
│   │   │   ├── my-courses.component.ts
│   │   │   └── my-courses.component.html
│   │   ├── course-progress/
│   │   │   ├── course-progress.component.ts
│   │   │   └── course-progress.component.html
│   │   ├── enrollment-history/
│   │   │   ├── enrollment-history.component.ts
│   │   │   └── enrollment-history.component.html
│   │   └── certificate-view/
│   │       ├── certificate-view.component.ts
│   │       └── certificate-view.component.html
│   │
│   ├── components/
│   │   ├── enrollment-card/
│   │   │   ├── enrollment-card.component.ts
│   │   │   └── enrollment-card.component.html
│   │   ├── progress-bar/
│   │   │   └── progress-bar.component.ts
│   │   ├── module-accordion/
│   │   │   └── module-accordion.component.ts
│   │   └── cancel-confirm-modal/
│   │       └── cancel-confirm-modal.component.ts
│   │
│   └── services/
│       └── enrollments-api.service.ts  # Extended enrollment + progress + certificates
```

---

## 2. List of Components

| Component | Purpose |
|-----------|---------|
| `EnrollmentsComponent` | Shell/layout for enrollments section |
| `MyCoursesComponent` | Main dashboard – grid/list of enrolled courses |
| `CourseProgressComponent` | Course progress view – modules, lessons, resume |
| `EnrollmentHistoryComponent` | Past enrollments with filters |
| `CertificateViewComponent` | PDF preview, download, share |
| `EnrollmentCardComponent` | Reusable course card (thumbnail, progress, actions) |
| `ProgressBarComponent` | Animated progress bar |
| `ModuleAccordionComponent` | Accordion for modules and lessons |
| `CancelConfirmModalComponent` | Confirmation dialog for cancel enrollment |

---

## 3. UI Elements Shown to Users

### My Courses (Main Page)

- Page title: “My Courses”
- Grid / list view toggle
- Course cards:
  - Course thumbnail
  - Course title
  - Instructor name
  - Enrollment date
  - Progress bar (animated)
  - Status badge (In Progress / Completed / Cancelled)
  - “Continue Learning” button
  - “View Certificate” button (when completed)
  - “Cancel Enrollment” button
- Empty state: “No enrolled courses yet”
- Loading skeletons
- “Continue where you left off” section (personalization)

### Course Progress View

- Current progress percentage (sticky)
- Modules (accordion)
- Lessons per module (with check / lock icons)
- Current lesson highlighted
- “Mark as complete” button
- Estimated time remaining

### Enrollment History

- List of past enrollments
- Completed / Cancelled badges
- Completion date
- Certificate download link
- Filters: Completed | In Progress | Cancelled

### Certificate View

- Certificate preview (PDF)
- Download button
- Share button
- Completion date
- Achievement badge / indicator

### Personalization

- Greeting: “Welcome back, {name}!”
- “Continue where you left off” section
- Stats: total enrolled, completed, learning hours

---

## 4. UX Behavior Description

| Feature | Behavior |
|---------|----------|
| **View toggle** | Switch between grid and list layout |
| **Progress bar** | Animated fill on load and update |
| **Completed cards** | Green accent, badge, “View Certificate” visible |
| **Hover effects** | Smooth shadow and scale on cards |
| **Cancel** | Modal: “Are you sure?” → confirm / cancel |
| **Continue Learning** | Navigate to Course Progress; resume last lesson |
| **Accordion** | Expand/collapse modules with animation |
| **Lesson complete** | API call, then real-time progress update |
| **Responsive** | Breakpoints for mobile, tablet, desktop |
| **Theme** | Dark/light toggle with CSS variables |
| **Toasts** | Success/error feedback for actions |

---

## 5. Design Style

- **LMS style**: Clean, modern, card-based
- **Colors**: Violet primary, green for completed, slate/gray neutrals
- **Spacing**: Generous padding, clear hierarchy
- **Typography**: Sans-serif, clear headings and labels
- **Animations**: Subtle, fast (≈300ms)

---

## 6. Best Practices for Performance & Scalability

- **Lazy loading**: Enrollments routes lazy-loaded
- **Change detection**: `OnPush` where possible
- **TrackBy**: Use in `*ngFor` for lists
- **Interceptors**: JWT and error handling
- **Guards**: `AuthGuard` and `RoleGuard` (STUDENT)
- **Reusable components**: `EnrollmentCard`, `ProgressBar`, etc.
- **Services**: Single API service for enrollments/progress/certificates
- **Responsive**: Tailwind breakpoints (sm, md, lg)
