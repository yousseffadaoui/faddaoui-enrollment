import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-4 right-4 z-[9999] flex flex-col gap-2 max-w-sm">
      @for (t of toast.messages(); track t.id) {
        <div
          class="px-4 py-3 rounded-xl shadow-lg text-white flex items-center justify-between gap-4"
          [class.bg-emerald-600]="t.type === 'success'"
          [class.bg-red-600]="t.type === 'error'"
          [class.bg-slate-700]="t.type === 'info'"
        >
          <span>{{ t.message }}</span>
          <button (click)="toast.dismiss(t.id)" class="shrink-0 text-white/80 hover:text-white">×</button>
        </div>
      }
    </div>
  `
})
export class ToastContainerComponent {
  constructor(public toast: ToastService) {}
}
