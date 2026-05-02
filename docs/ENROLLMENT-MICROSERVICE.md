# Enrollment Microservice – Architecture & API Specification

## 1. Folder Structure

```
enrollment-service/
├── src/main/java/com/englishschool/enrollmentservice/
│   ├── EnrollmentServiceApplication.java
│   ├── config/              # Redis, Security
│   ├── controller/          # REST controllers
│   │   ├── EnrollmentController.java
│   │   ├── ProgressController.java
│   │   └── CertificateController.java
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── event/               # Kafka consumers & publishers
│   ├── client/              # Feign client to Course MS
│   └── security/
├── src/main/resources/
└── Dockerfile
```

## 2. Database Schema (PostgreSQL)

```sql
-- Enrollments
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(20) CHECK (status IN ('ACTIVE','COMPLETED','DROPPED','SUSPENDED')) DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    UNIQUE(user_id, course_id)
);

-- Progress (lesson completion)
CREATE TABLE progress (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(enrollment_id, lesson_id)
);

-- Certificates
CREATE TABLE certificates (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE UNIQUE,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    certificate_url VARCHAR(500)
);

CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_course ON enrollments(course_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_progress_enrollment ON progress(enrollment_id);
```

## 3. REST API Endpoints (v1)

### Enrollments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/enrollments | List enrollments (user/Admin) |
| GET | /api/v1/enrollments/user/{userId} | User's enrollments |
| GET | /api/v1/enrollments/{id} | Get enrollment |
| POST | /api/v1/enrollments | Enroll (idempotent) |
| PUT | /api/v1/enrollments/{id} | Update status (Admin) |
| DELETE | /api/v1/enrollments/{id} | Drop enrollment |

### Progress
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/enrollments/{id}/progress | Get progress % |
| POST | /api/v1/enrollments/{id}/lessons/{lessonId}/complete | Mark lesson complete |

### Certificates
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/certificates/user/{userId} | User's certificates |
| GET | /api/v1/enrollments/{id}/certificate | Get certificate for enrollment |

### Recommendations
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/recommendations/user/{userId} | Recommended courses |
