import { Component, OnInit, ChangeDetectorRef, AfterViewInit, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { EnrollmentService } from '../services/enrollment.service';

@Component({
  selector: 'app-enrollment',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './enrollment.component.html',
  styleUrls: ['./enrollment.component.css']
})
export class EnrollmentComponent implements OnInit, AfterViewInit {
  private platformId = inject(PLATFORM_ID);

  enrollments: any[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private enrollmentService: EnrollmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEnrollments();
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      import('gsap').then(({ gsap }) => {
        gsap.from('.enrollment-card', { opacity: 0, y: 40, duration: 0.6, stagger: 0.1 });
      });
    }
  }

  loadEnrollments(): void {
    this.loading = true;
    this.enrollmentService.getEnrollments().subscribe({
      next: (data) => {
        this.enrollments = data;
        this.loading = false;
        this.cdr.markForCheck();
        console.log("Enrollments received:", data);
      },
      error: (err) => {
        this.loading = false;
        this.error = "Error loading enrollments";
        this.cdr.markForCheck();
        console.error("Error loading enrollments:", err);
      }
    });
  }

}
