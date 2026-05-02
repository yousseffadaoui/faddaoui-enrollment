# ESM LMS – Complete Production-Grade Architecture

## 1. Full Project Structure

### Backend (Spring Boot Microservices)

```
Faddaoui/
├── api-gateway/                    # Spring Cloud Gateway
│   ├── src/main/java/.../config/
│   ├── src/main/resources/application.yml
│   └── Dockerfile
│
├── config-server/                  # Spring Cloud Config (optional)
│
├── course-service/
│   ├── src/main/java/com/englishschool/courseservice/
│   │   ├── config/                 # Redis, Kafka, Security
│   │   ├── controller/             # CourseController, CategoryController, etc.
│   │   ├── dto/                    # CourseDTO, ModuleDTO, LessonDTO...
│   │   ├── entity/                 # Course, Module, Lesson, Category, Review, Instructor
│   │   ├── mapper/                 # MapStruct mappers
│   │   ├── repository/
│   │   ├── service/
│   │   ├── exception/              # GlobalExceptionHandler
│   │   └── event/                  # Kafka producer
│   └── pom.xml
│
├── enrollment-service/
│   ├── src/main/java/com/englishschool/enrollmentservice/
│   │   ├── client/                 # Feign CourseClient
│   │   ├── config/                 # Feign, Kafka, Security
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── service/                # CertificateService (PDF)
│   │   └── consumer/               # Kafka consumer
│   └── pom.xml
│
├── esm-front-main/                 # Angular LMS
│   ├── src/app/
│   │   ├── core/                   # Auth, interceptors, guards
│   │   ├── shared/                 # SharedModule
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   ├── courses/
│   │   │   ├── enrollments/
│   │   │   ├── dashboard/
│   │   │   └── admin/
│   │   └── layouts/
│   └── package.json
│
└── docker-compose.yml
```

### Angular Module Structure

```
esm-front-main/src/app/
├── core/                           # Singleton services
│   ├── auth/auth.service.ts
│   ├── auth/auth.guard.ts
│   ├── auth/role.guard.ts
│   ├── interceptors/jwt.interceptor.ts
│   └── interceptors/error.interceptor.ts
├── shared/                         # Reusable components
│   ├── components/loading-skeleton/
│   ├── components/progress-bar/
│   └── pipes/
├── features/
│   ├── auth/                       # Login, Register
│   ├── courses/                    # Catalog, Details, Preview
│   ├── enrollments/                # My Courses, Continue Learning
│   ├── dashboard/                  # Personalized dashboard
│   └── admin/                      # Admin CRUD
└── layouts/                        # MainLayout, AuthLayout
```

---

## 2. API Endpoints Reference

### Course Microservice (`http://localhost:8083/api/v1`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /courses | List (paginated, filter: level, category, search) | Public |
| GET | /courses/{id} | Get course (preview) | Public |
| POST | /courses | Create course | ADMIN |
| PUT | /courses/{id} | Update course | ADMIN |
| DELETE | /courses/{id} | Delete course | ADMIN |
| PATCH | /courses/{id}/publish | Publish course | ADMIN |
| GET | /categories | List categories | Public |
| GET | /courses/{id}/modules | Get modules + lessons | Public |
| GET | /courses/{id}/reviews | List reviews (paginated) | Public |
| POST | /courses/{id}/reviews | Add review | STUDENT |

### Enrollment Microservice (`http://localhost:8084/api/v1`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /enrollments | Enroll (idempotent, X-Idempotency-Key) | STUDENT |
| DELETE | /enrollments/{id} | Cancel enrollment | STUDENT |
| GET | /enrollments/me | My enrollments (dashboard) | STUDENT |
| GET | /enrollments/{id}/progress | Get progress % | STUDENT |
| POST | /enrollments/{id}/lessons/{lessonId}/complete | Mark lesson complete | STUDENT |
| GET | /enrollments/{id}/certificate | Download certificate PDF | STUDENT |

---

## 3. Security (JWT)

### Flow
1. `POST /api/v1/auth/login` → returns JWT
2. Frontend stores JWT (localStorage/sessionStorage)
3. HTTP Interceptor adds `Authorization: Bearer <token>`
4. Backend validates JWT + extracts roles (ADMIN, STUDENT, INSTRUCTOR)

### Key Dependencies (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
```

---

## 4. Tech Stack Summary

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.2, Spring Security 6, JWT |
| DB | PostgreSQL (prod), H2 (dev) |
| Cache | Redis |
| Messaging | Kafka |
| Inter-service | OpenFeign |
| Mapping | MapStruct |
| Docs | OpenAPI 3 / Swagger |
| Frontend | Angular 21, Tailwind |
| State | NgRx or Signals |
| Container | Docker, Kubernetes |
| CI/CD | GitHub Actions |

---

## 5. Implementation Phases

**Phase 1 (Week 1-2):** DTOs, MapStruct, Global Exception Handler, API versioning, Validation  
**Phase 2 (Week 3-4):** Security (JWT), Redis caching, Kafka producer/consumer  
**Phase 3 (Week 5-6):** Feign client, Certificate generation, Full CRUD  
**Phase 4 (Week 7-8):** Angular auth, guards, dashboard, progress UI  
**Phase 5 (Week 9-10):** API Gateway, Docker, CI/CD, K8s
