import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProgressBarComponent } from '../progress-bar/progress-bar.component';
import type { MyCourse } from '../../../services/enrollments-api.service';
import { normalizeThumbnailUrl } from '../../../utils/thumbnail';

@Component({
  selector: 'app-enrollment-card',
  standalone: true,
  imports: [CommonModule, RouterLink, ProgressBarComponent],
  templateUrl: './enrollment-card.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EnrollmentCardComponent {
  @Input() course!: MyCourse;
  @Input() instructorName?: string;
  @Input() viewMode: 'grid' | 'list' = 'grid';

  @Output() cancelClick = new EventEmitter<number>();
  @Output() deleteClick = new EventEmitter<number>();
  @Output() enrollClick = new EventEmitter<number>();

  onCancel(): void {
    this.cancelClick.emit(this.course.enrollmentId);
  }

  onDelete(): void {
    this.deleteClick.emit(this.course.enrollmentId);
  }

  onEnroll(): void {
    const id = this.course.courseId ?? this.course.course?.courseId;
    if (id != null && id !== 0) {
      this.enrollClick.emit(id);
    }
  }

  get statusLabel(): string {
    switch (this.course.status) {
      case 'completed': return 'Completed';
      case 'cancelled': return 'Cancelled';
      case 'not_enrolled': return 'Not enrolled';
      default: return 'In Progress';
    }
  }

  get statusClass(): string {
    switch (this.course.status) {
      case 'completed': return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400';
      case 'cancelled': return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400';
      case 'not_enrolled': return 'bg-gray-100 text-gray-600 dark:bg-slate-800/40 dark:text-slate-300';
      default: return 'bg-violet-100 text-violet-700 dark:bg-violet-900/30 dark:text-violet-400';
    }
  }

  get isCompleted(): boolean {
    return this.course.status === 'completed';
  }

  get thumbnail(): string {
    const url = normalizeThumbnailUrl(this.course.course?.thumbnailUrl);
    return url || 'https://placehold.co/400x220/8b5cf6/white?text=Course';
  }
}
