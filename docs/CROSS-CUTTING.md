# Cross-Cutting Concerns – API Gateway, Auth, Events, Observability

## 1. API Gateway (Spring Cloud Gateway)

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: course-service
          uri: lb://COURSE-SERVICE
          predicates:
            - Path=/api/v1/courses/**, /api/v1/categories/**, /api/v1/modules/**, /api/v1/lessons/**, /api/v1/reviews/**, /api/v1/instructors/**
        - id: enrollment-service
          uri: lb://ENROLLMENT-SERVICE
          predicates:
            - Path=/api/v1/enrollments/**, /api/v1/progress/**, /api/v1/certificates/**, /api/v1/recommendations/**
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
      globalcors:
        add-to-simple-url-handler-mapping: true
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: GET, POST, PUT, DELETE, OPTIONS
```

## 2. JWT Authentication Flow

```
Client → POST /auth/login (email, password) → JWT token
Client → API call with Header: Authorization: Bearer <token>
Gateway/Service → Validate JWT → Extract roles → RBAC
```

## 3. RBAC Roles

| Role | Permissions |
|------|-------------|
| STUDENT | Enroll, view courses, submit reviews, track progress |
| INSTRUCTOR | Create/edit own courses, modules, lessons |
| ADMIN | Full CRUD on all resources, manage users |

## 4. Event Bus (Kafka Topics)

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| course.created | Course MS | Enrollment MS (recommendations) | courseId, title, category |
| course.updated | Course MS | Enrollment MS | courseId, changes |
| course.deleted | Course MS | Enrollment MS | courseId |
| enrollment.created | Enrollment MS | Course MS (analytics) | enrollmentId, courseId, userId |
| enrollment.completed | Enrollment MS | Course MS, User MS | enrollmentId, certificate |
| lesson.completed | Enrollment MS | Course MS | enrollmentId, lessonId |

## 5. Circuit Breaker (Resilience4j)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      courseService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        failureRateThreshold: 50
```

## 6. Rate Limiting

- API Gateway: 100 req/min per IP (default)
- Per user: 1000 req/min (authenticated)
