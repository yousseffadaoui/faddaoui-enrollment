import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { EnrollmentsApiService } from '../../../services/enrollments-api.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-certificate-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './certificate-view.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CertificateViewComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(EnrollmentsApiService);
  private toast = inject(ToastService);
  private sanitizer = inject(DomSanitizer);
  private cdr = inject(ChangeDetectorRef);

  enrollmentId = 0;
  pdfUrl: SafeResourceUrl | null = null;
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.enrollmentId = Number(this.route.snapshot.paramMap.get('enrollmentId'));
    this.api.downloadCertificate(this.enrollmentId).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: err => {
        this.loading = false;
        let msg = 'Failed to load certificate.';
        if (err?.status === 404) msg = 'Enrollment not found.';
        else if (err?.status === 400 || err?.message?.includes('completed')) msg = 'Complete the course first to earn a certificate.';
        else if (typeof err?.error === 'object' && err?.error?.message) msg = err.error.message;
        else if (typeof err?.error === 'string') msg = err.error;
        this.error = msg;
        this.cdr.markForCheck();
      }
    });
  }

  download(): void {
    this.api.downloadCertificate(this.enrollmentId).subscribe({
      next: blob => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'certificate-' + this.enrollmentId + '.pdf';
        a.click();
        URL.revokeObjectURL(a.href);
        this.toast.success('Certificate downloaded.');
      },
      error: () => this.toast.error('Download failed.')
    });
  }

  share(): void {
    this.api.downloadCertificate(this.enrollmentId).subscribe({
      next: blob => {
        const file = new File([blob], 'certificate.pdf', { type: 'application/pdf' });
        if (navigator.share) {
          navigator.share({ title: 'My Certificate', files: [file] })
            .then(() => this.toast.success('Certificate shared.'))
            .catch(() => this.toast.info('Sharing not supported. Use Download.'));
        } else {
          this.toast.info('Sharing not supported. Use Download.');
        }
      },
      error: () => this.toast.error('Share failed.')
    });
  }
}
