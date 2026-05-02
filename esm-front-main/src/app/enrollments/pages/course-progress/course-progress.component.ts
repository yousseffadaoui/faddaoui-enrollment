import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { EnrollmentsApiService } from '../../../services/enrollments-api.service';
import { CoursesService, Lesson, Module } from '../../../services/courses.service';
import { ToastService } from '../../../services/toast.service';
import { ProgressBarComponent } from '../../components/progress-bar/progress-bar.component';
import { ENGLISH_LEVEL_QUIZ, EnglishQuizQuestion, QuizOptionKey } from './english-level-quiz';

@Component({
  selector: 'app-course-progress',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressBarComponent],
  templateUrl: './course-progress.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CourseProgressComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(EnrollmentsApiService);
  private coursesSvc = inject(CoursesService);
  private toast = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);
  private sanitizer = inject(DomSanitizer);

  enrollmentId = 0;
  courseId = 0;
  courseName = '';
  progressPercent = 0;
  completedLessonIds = new Set<number>();
  modules: Module[] = [];
  totalMinutes = 0;
  completedMinutes = 0;
  loading = true;
  error: string | null = null;
  expandedModuleIds = new Set<number>();
  selectedLesson: Lesson | null = null;

  // Built‑in English level quiz state
  quizQuestions: EnglishQuizQuestion[] = ENGLISH_LEVEL_QUIZ;
  optionKeys: QuizOptionKey[] = ['A', 'B', 'C', 'D'];
  certificateMinCorrect = 10;
  quizStarted = false;
  quizFinished = false;
  currentQuestionIndex = 0;
  // questionId -> selected option key
  selectedOptions = new Map<number, QuizOptionKey>();
  quizScorePercent = 0;
  quizLevelLabel = '';
  quizCorrectCount = 0;

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        switchMap(params => {
          const id = Number(params.get('enrollmentId'));
          this.enrollmentId = id;
          if (!id || isNaN(id)) {
            throw new Error('Invalid enrollment ID');
          }
          return this.api.getEnrollment(id);
        })
      )
      .subscribe({
        next: enr => {
          const cid = enr?.courseId;
          this.courseId = typeof cid === 'number' ? cid : Number(cid) || 0;
          if (!this.courseId) {
            this.loading = false;
            this.error = 'Enrollment has no course.';
            this.cdr.markForCheck();
            return;
          }
          this.courseName = (enr as any)?.course?.name || (enr as any)?.courseName || 'Course';
          this.loadModules();
          this.loadProgress();
        },
        error: err => {
          this.loading = false;
          const status = err?.status ?? err?.error?.status;
          if (status === 404) {
            this.error = 'This enrollment no longer exists. It may have been cancelled or removed. Go back to My Courses to view your current enrollments.';
          } else {
            this.error = err?.error?.message || err?.message || 'Failed to load enrollment. Check that services are running (enrollment: 8084, course: 8083).';
          }
          this.cdr.markForCheck();
        }
      });
  }

  loadModules(): void {
    this.coursesSvc.getModules(this.courseId, true).subscribe({
      next: mods => {
        this.modules = mods || [];
        this.computeTime();
        this.loading = false;
        this.cdr.markForCheck();
        this.coursesSvc.getCourse(this.courseId).subscribe({
          next: c => {
            if (c?.name) this.courseName = c.name;
            this.cdr.markForCheck();
          },
          error: () => { this.courseName = this.courseName || 'Course'; this.cdr.markForCheck(); }
        });
      },
      error: err => {
        this.loading = false;
        this.modules = [];
        this.error = err?.error?.message || err?.message || 'Failed to load course modules. Ensure course-service (8083) is running.';
        this.courseName = this.courseName || 'Course';
        this.cdr.markForCheck();
      }
    });
  }

  loadProgress(): void {
    this.api.getProgressPercent(this.enrollmentId).subscribe({
      next: p => {
        this.progressPercent = p.progressPercent ?? 0;
        this.cdr.markForCheck();
      }
    });
    this.api.getCompletedLessons(this.enrollmentId).subscribe({
      next: list => {
        this.completedLessonIds = new Set((list || []).map(l => l.lessonId));
        this.computeTime();
        this.cdr.markForCheck();
      }
    });
  }

  toggleModule(id: number): void {
    if (this.expandedModuleIds.has(id)) {
      this.expandedModuleIds.delete(id);
    } else {
      this.expandedModuleIds.add(id);
    }
    this.expandedModuleIds = new Set(this.expandedModuleIds);
    this.cdr.markForCheck();
  }

  isExpanded(id: number): boolean {
    return this.expandedModuleIds.has(id);
  }

  selectLesson(lesson: Lesson): void {
    this.selectedLesson = lesson;
    this.cdr.markForCheck();
  }

  closeLesson(): void {
    this.selectedLesson = null;
    this.cdr.markForCheck();
  }

  /** Safe video URL for embedding (YouTube, Vimeo, or direct video) */
  getVideoEmbedUrl(url: string | undefined): string | null {
    if (!url?.trim()) return null;
    const u = url.trim();
    // YouTube: watch, youtu.be, embed, or /v/
    if (u.includes('youtube.com/watch') || u.includes('youtube.com/v/')) {
      const m = u.match(/[?&]v=([^&]+)/);
      return m ? `https://www.youtube-nocookie.com/embed/${m[1]}?rel=0` : null;
    }
    if (u.includes('youtu.be/')) {
      const m = u.match(/youtu\.be\/([^/?]+)/);
      return m ? `https://www.youtube-nocookie.com/embed/${m[1]}?rel=0` : null;
    }
    if (u.includes('youtube.com/embed/')) {
      const m = u.match(/youtube\.com\/embed\/([^?&]+)/);
      return m ? `https://www.youtube-nocookie.com/embed/${m[1]}?rel=0` : u;
    }
    if (u.includes('vimeo.com')) {
      const m = u.match(/vimeo\.com\/(?:video\/)?(\d+)/);
      return m ? `https://player.vimeo.com/video/${m[1]}` : null;
    }
    return u; // direct .mp4 etc.
  }

  getSafeVideoUrl(url: string | undefined): SafeResourceUrl | null {
    const u = this.getVideoEmbedUrl(url);
    return u ? this.sanitizer.bypassSecurityTrustResourceUrl(u) : null;
  }

  isVideoUrl(url: string | undefined): boolean {
    if (!url) return false;
    const u = url.toLowerCase();
    return u.includes('youtube') || u.includes('youtu.be') || u.includes('vimeo') ||
      u.endsWith('.mp4') || u.endsWith('.webm') || u.endsWith('.ogg');
  }

  markComplete(lessonId: number): void {
    this.api.markLessonComplete(this.enrollmentId, lessonId).subscribe({
      next: () => {
        this.completedLessonIds.add(lessonId);
        this.completedLessonIds = new Set(this.completedLessonIds);
        this.loadProgress();
        this.toast.success('Lesson marked complete.');
        this.cdr.markForCheck();
      },
      error: err => this.toast.error(err?.error?.message || 'Failed to mark complete.')
    });
  }

  isCompleted(lessonId: number): boolean {
    return this.completedLessonIds.has(lessonId);
  }

  get estimatedMinutesRemaining(): number {
    return Math.max(0, this.totalMinutes - this.completedMinutes);
  }

  private computeTime(): void {
    let total = 0;
    let done = 0;
    this.modules.forEach(m => {
      (m.lessons || []).forEach(l => {
        const min = l.durationMinutes ?? 0;
        total += min;
        if (l.id && this.completedLessonIds.has(l.id)) done += min;
      });
    });
    this.totalMinutes = total;
    this.completedMinutes = done;
  }

  // ===== Quiz helpers =====
  startQuiz(): void {
    this.quizStarted = true;
    this.quizFinished = false;
    this.currentQuestionIndex = 0;
  }

  selectOption(q: EnglishQuizQuestion, key: QuizOptionKey): void {
    this.selectedOptions.set(q.id, key);
  }

  isSelected(q: EnglishQuizQuestion, key: QuizOptionKey): boolean {
    return this.selectedOptions.get(q.id) === key;
  }

  nextQuestion(): void {
    if (this.currentQuestionIndex < this.quizQuestions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  prevQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  submitQuiz(): void {
    const total = this.quizQuestions.length;
    let correct = 0;
    this.quizQuestions.forEach(q => {
      if (this.selectedOptions.get(q.id) === q.correct) correct++;
    });
    this.quizCorrectCount = correct;
    this.quizScorePercent = total ? Math.round((correct / total) * 100) : 0;

    if (this.quizScorePercent >= 90) this.quizLevelLabel = 'C2 (Proficient)';
    else if (this.quizScorePercent >= 80) this.quizLevelLabel = 'C1 (Advanced)';
    else if (this.quizScorePercent >= 65) this.quizLevelLabel = 'B2 (Upper‑intermediate)';
    else if (this.quizScorePercent >= 50) this.quizLevelLabel = 'B1 (Intermediate)';
    else if (this.quizScorePercent >= 35) this.quizLevelLabel = 'A2 (Elementary)';
    else this.quizLevelLabel = 'A1 (Beginner)';

    this.quizFinished = true;
  }

  resetQuiz(): void {
    this.quizStarted = false;
    this.quizFinished = false;
    this.currentQuestionIndex = 0;
    this.quizScorePercent = 0;
    this.quizLevelLabel = '';
    this.quizCorrectCount = 0;
    this.selectedOptions = new Map<number, QuizOptionKey>();
  }
}
