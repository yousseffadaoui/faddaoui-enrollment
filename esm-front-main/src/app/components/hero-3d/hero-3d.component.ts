import {
  Component,
  ElementRef,
  AfterViewInit,
  OnDestroy,
  PLATFORM_ID,
  inject,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hero-3d',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './hero-3d.component.html',
  styleUrls: ['./hero-3d.component.css'],
})
export class Hero3dComponent implements AfterViewInit, OnDestroy {
  private el = inject(ElementRef);
  private platformId = inject(PLATFORM_ID);

  private renderer: any;
  private scene: any;
  private camera: any;
  private mesh: any;
  private mesh2: any;
  private mesh3: any;
  private frameId = 0;

  ngAfterViewInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.initThree();
    }
  }

  ngOnDestroy() {
    if (this.frameId) cancelAnimationFrame(this.frameId);
    if (this.renderer) this.renderer.dispose();
  }

  private async initThree() {
    const { Scene, PerspectiveCamera, WebGLRenderer, Mesh, SphereGeometry, MeshBasicMaterial, Color } = await import('three');
    const container = this.el.nativeElement.querySelector('.hero-3d-canvas');
    if (!container) return;

    const width = container.clientWidth;
    const height = container.clientHeight;

    this.scene = new Scene();
    this.scene.background = new Color(0x0a0a1a);

    this.camera = new PerspectiveCamera(75, width / height, 0.1, 1000);
    this.camera.position.z = 5;

    this.renderer = new WebGLRenderer({ alpha: true, antialias: true });
    this.renderer.setSize(width, height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    container.appendChild(this.renderer.domElement);

    const geometry = new SphereGeometry(0.5, 32, 32);
    const material = new MeshBasicMaterial({ color: 0x5f2ded, transparent: true, opacity: 0.15 });
    this.mesh = new Mesh(geometry, material);
    this.mesh.position.set(2, 0, -2);
    this.scene.add(this.mesh);

    const material2 = new MeshBasicMaterial({ color: 0xf2277e, transparent: true, opacity: 0.1 });
    this.mesh2 = new Mesh(geometry, material2);
    this.mesh2.position.set(-2, 1, -3);
    this.scene.add(this.mesh2);

    const material3 = new MeshBasicMaterial({ color: 0x03a9f4, transparent: true, opacity: 0.08 });
    this.mesh3 = new Mesh(geometry, material3);
    this.mesh3.position.set(0, -1.5, -4);
    this.scene.add(this.mesh3);

    const handleResize = () => {
      const w = container.clientWidth;
      const h = container.clientHeight;
      this.camera.aspect = w / h;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(w, h);
    };
    window.addEventListener('resize', handleResize);

    const animate = (time: number) => {
      this.frameId = requestAnimationFrame(animate);
      this.mesh.rotation.y = time * 0.0003;
      this.mesh2.rotation.x = time * 0.0002;
      this.mesh3.rotation.y = time * 0.0004;
      this.renderer.render(this.scene, this.camera);
    };
    animate(0);
  }
}
