import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-progress-bar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2 overflow-hidden" role="progressbar">
      <div
        class="h-full rounded-full transition-all duration-700 ease-out"
        [class.bg-emerald-500]="percent >= 100"
        [class.bg-violet-500]="percent < 100"
        [style.width.%]="percent"
      ></div>
    </div>
    @if (showLabel) {
      <span class="text-sm font-medium text-gray-600 dark:text-gray-400 mt-1">{{ percent }}%</span>
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProgressBarComponent {
  @Input() percent = 0;
  @Input() showLabel = false;
}
