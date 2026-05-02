# ESM – Enterprise LMS Architecture Deliverables

This folder contains the complete architectural design and foundational code for transforming the ESM (English School Management) platform into an enterprise-grade, cloud-ready LMS.

## 📁 Deliverables Index

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | High-level architecture diagrams (Mermaid), tech stack, microservice flow |
| [COURSE-MICROSERVICE.md](./COURSE-MICROSERVICE.md) | Course MS folder structure, DB schema, REST API spec |
| [ENROLLMENT-MICROSERVICE.md](./ENROLLMENT-MICROSERVICE.md) | Enrollment MS folder structure, DB schema, REST API spec |
| [CROSS-CUTTING.md](./CROSS-CUTTING.md) | API Gateway, JWT, RBAC, Kafka events, Circuit Breaker, Rate Limiting |
| [API-ENDPOINTS.md](./API-ENDPOINTS.md) | Full API reference for both microservices |
| [IMPLEMENTATION-ROADMAP.md](./IMPLEMENTATION-ROADMAP.md) | Phased implementation plan (10 weeks) |

## 🏗️ Implemented Foundation

### Course Microservice
- **Entities:** Category, Module, Lesson, Review (added)
- **Course:** Extended with category, price, thumbnail, rating
- **Repositories:** CategoryRepository, ModuleRepository, LessonRepository, ReviewRepository
- **Dockerfile** for containerization

### Enrollment Microservice
- **Entities:** Progress, Certificate (added)
- **Enrollment:** Extended with userId, enrolledAt, completedAt
- **Repositories:** ProgressRepository, CertificateRepository
- **Dockerfile** for containerization

### Infrastructure
- **docker-compose.yml** – Eureka, Course MS, Enrollment MS, Redis, PostgreSQL x2
- **Dockerfiles** for each Spring Boot service

## 🚀 Quick Start

### Development (local)
1. Start Eureka: `cd Eureka-main && ./mvnw spring-boot:run`
2. Start Course Service: `cd course-service && ./mvnw spring-boot:run`
3. Start Enrollment Service: `cd Enrollment-service && ./mvnw spring-boot:run`
4. Start Frontend: `cd esm-front-main && ng serve`

### Docker
```bash
docker-compose up -d
```

## 📋 Next Steps (Phased Implementation)

1. **Phase 1** – Flyway migrations, API versioning `/api/v1/`, PostgreSQL config
2. **Phase 2** – Category/Module/Lesson CRUD controllers, Progress API
3. **Phase 3** – JWT auth, Kafka events, idempotency
4. **Phase 4** – Redis caching, API Gateway, CI/CD
5. **Phase 5** – ELK, Prometheus/Grafana, UX enhancements
