import { Component, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cancel-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" (click)="cancel.emit()">
      <div class="bg-white dark:bg-slate-800 rounded-2xl shadow-xl max-w-md w-full p-6" (click)="$event.stopPropagation()">
        <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-2">Cancel enrollment?</h3>
        <p class="text-gray-600 dark:text-gray-400 mb-6">Your progress will be saved. You can re-enroll later if needed.</p>
        <div class="flex gap-3 justify-end">
          <button (click)="cancel.emit()" class="px-4 py-2 rounded-xl border border-gray-200 dark:border-slate-600 text-gray-700 dark:text-gray-300 font-medium hover:bg-gray-50 dark:hover:bg-slate-700">Keep</button>
          <button (click)="confirm.emit()" class="px-4 py-2 rounded-xl bg-amber-600 text-white font-semibold hover:bg-amber-700">Cancel enrollment</button>
        </div>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CancelConfirmModalComponent {
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
