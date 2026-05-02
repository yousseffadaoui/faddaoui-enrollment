# ESM Implementation Roadmap

## Phase 1: Foundation (Weeks 1–2)
- [ ] Course MS: Add Category, Module, Lesson entities
- [ ] Course MS: Flyway migrations for new schema
- [ ] Enrollment MS: Add Progress, Certificate entities
- [ ] Both: API versioning `/api/v1/`
- [ ] Both: OpenAPI/Swagger config
- [ ] PostgreSQL setup (replace H2 for prod)

## Phase 2: Core CRUD (Weeks 3–4)
- [ ] Course MS: Category CRUD
- [ ] Course MS: Module & Lesson CRUD
- [ ] Course MS: Review CRUD
- [ ] Enrollment MS: Progress tracking API
- [ ] Enrollment MS: Certificate generation
- [ ] Pagination on list endpoints
- [ ] Search & filters (level, category)

## Phase 3: Security & Events (Weeks 5–6)
- [ ] JWT authentication (shared User Service or Auth MS)
- [ ] Spring Security + RBAC
- [ ] Kafka/RabbitMQ setup
- [ ] Event publishers in Course & Enrollment MS
- [ ] Idempotency keys on enrollment

## Phase 4: Performance & DevOps (Weeks 7–8)
- [ ] Redis caching (course catalog, recommendations)
- [ ] Circuit Breaker (Resilience4j)
- [ ] API Gateway (Spring Cloud Gateway)
- [ ] Docker & docker-compose
- [ ] GitHub Actions CI/CD

## Phase 5: Observability & UX (Weeks 9–10)
- [ ] ELK / centralized logging
- [ ] Prometheus + Grafana
- [ ] Frontend: My Courses dashboard
- [ ] Frontend: Progress bar, Continue Learning
- [ ] Notifications (email placeholder)
