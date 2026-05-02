import {
  Component,
  AfterViewInit,
  PLATFORM_ID,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { bootstrapCheck } from '@ng-icons/bootstrap-icons';
import { Hero3dComponent } from '../components/hero-3d/hero-3d.component';

@Component({
  selector: 'app-content',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NgIconComponent,
    Hero3dComponent,
  ],
  providers: [provideIcons({ bootstrapCheck })],
  templateUrl: './content.html',
  styleUrls: ['./content.css'],
})
export class Content implements AfterViewInit {
  private platformId = inject(PLATFORM_ID);

  ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      import('gsap').then(({ gsap }) => {
        const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
        tl.from('.hero-title', { opacity: 0, y: 60, duration: 0.8 })
          .from('.hero-subtitle', { opacity: 0, y: 40, duration: 0.6 }, '-=0.4')
          .from('.hero-buttons', { opacity: 0, y: 30, duration: 0.5 }, '-=0.3')
          .from('.section-card', { opacity: 0, y: 50, duration: 0.6, stagger: 0.15 }, '-=0.2');
        gsap.utils.toArray('.stat-item').forEach((el: any, i: number) => {
          gsap.from(el, { opacity: 0, scale: 0.8, duration: 0.5, delay: 1.2 + i * 0.1 });
        });
      });
    }
  }
}
