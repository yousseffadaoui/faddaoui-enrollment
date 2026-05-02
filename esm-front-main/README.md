# ESM - English School Management

A modern cloud-based Learning Management System (LMS) for English language education, built with Angular 21, Three.js, and GSAP animations.

## Features

- **Modern Design** – Clean, responsive UI with violet/pink gradient accents
- **3D Hero Background** – Interactive Three.js floating orbs on the hero section
- **GSAP Animations** – Smooth scroll-triggered and entrance animations
- **Course Catalog** – Browse English courses by level (Beginner, Intermediate, Advanced)
- **Enrollment Tracking** – View and manage your enrolled courses
- **Microservices Backend** – Spring Boot services with Eureka discovery

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Angular 21, Tailwind CSS, GSAP, Three.js |
| Backend | Spring Boot 3, Eureka, H2 |
| Services | course-service (8083), enrollment-service (8084), Eureka (8761) |

## Getting Started

### Frontend

```bash
cd esm-front-main
npm install
ng serve
```

Open [http://localhost:4200](http://localhost:4200)

### Backend (optional – for Courses & Enrollments API)

1. **Eureka Server** (start first)
   ```bash
   cd Eureka-main
   ./mvnw spring-boot:run
   ```

2. **Course Service**
   ```bash
   cd course-service
   ./mvnw spring-boot:run
   ```

3. **Enrollment Service**
   ```bash
   cd Enrollment-service
   ./mvnw spring-boot:run
   ```

## Project Structure

```
esm-front-main/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   └── hero-3d/          # Three.js hero background
│   │   ├── content/              # Landing page
│   │   ├── courses/              # Course catalog
│   │   ├── enrollment/           # My enrollments
│   │   ├── header/
│   │   └── footer/
│   └── assets/
└── angular.json
```

## Build

```bash
ng build
```

Output: `dist/edutest/`

## Design Highlights

- **Hero Section** – Full-height hero with Three.js gradient orbs, GSAP entrance animations
- **Course Cards** – Hover effects, gradient accents, staggered GSAP load-in
- **Enrollment Cards** – Status badges (active/pending), clean card layout
- **Footer** – Dark theme with gradient branding
