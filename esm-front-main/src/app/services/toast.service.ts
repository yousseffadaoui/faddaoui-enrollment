import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
  duration?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private toasts = signal<Toast[]>([]);
  private nextId = 1;

  readonly messages = this.toasts.asReadonly();

  success(message: string, duration = 4000): void {
    this.add({ message, type: 'success', duration });
  }

  error(message: string, duration = 5000): void {
    this.add({ message, type: 'error', duration });
  }

  info(message: string, duration = 4000): void {
    this.add({ message, type: 'info', duration });
  }

  private add(t: Omit<Toast, 'id'>): void {
    const toast: Toast = { ...t, id: this.nextId++ };
    this.toasts.update(list => [...list, toast]);
    const d = t.duration ?? 4000;
    if (d > 0) {
      setTimeout(() => this.dismiss(toast.id), d);
    }
  }

  dismiss(id: number): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}
