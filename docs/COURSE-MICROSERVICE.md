# Course Microservice – Architecture & API Specification

## 1. Folder Structure

```
course-service/
├── src/main/java/com/englishschool/courseservice/
│   ├── CourseServiceApplication.java
│   ├── config/              # Redis, Security, Swagger
│   ├── controller/          # REST controllers
│   │   ├── CourseController.java
│   │   ├── CategoryController.java
│   │   ├── ModuleController.java
│   │   ├── LessonController.java
│   │   ├── ReviewController.java
│   │   └── InstructorController.java
│   ├── dto/                 # Request/Response DTOs
│   ├── entity/              # JPA entities
│   ├── repository/          # Spring Data repositories
│   ├── service/             # Business logic
│   ├── exception/           # Global exception handler
│   ├── event/               # Kafka event publishers
│   └── security/            # JWT filter, RBAC
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/        # Flyway scripts
└── Dockerfile
```

## 2. Database Schema (PostgreSQL)

```sql
-- Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    slug VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Instructors
CREATE TABLE instructors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Courses
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    category_id BIGINT REFERENCES categories(id),
    instructor_id BIGINT REFERENCES instructors(id),
    level VARCHAR(5) CHECK (level IN ('A1','A2','B1','B2','C1','C2')),
    price DECIMAL(10,2) DEFAULT 0,
    thumbnail_url VARCHAR(500),
    duration_hours INT DEFAULT 0,
    is_published BOOLEAN DEFAULT FALSE,
    rating_avg DECIMAL(3,2) DEFAULT 0,
    rating_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Modules
CREATE TABLE modules (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Lessons
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    content_type VARCHAR(20) CHECK (content_type IN ('VIDEO','PDF','QUIZ','TEXT')),
    content_url VARCHAR(500),
    duration_minutes INT DEFAULT 0,
    order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reviews
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(course_id, user_id)
);

CREATE INDEX idx_courses_category ON courses(category_id);
CREATE INDEX idx_courses_level ON courses(level);
CREATE INDEX idx_courses_rating ON courses(rating_avg);
CREATE INDEX idx_modules_course ON modules(course_id);
CREATE INDEX idx_lessons_module ON lessons(module_id);
```

## 3. REST API Endpoints (v1)

### Courses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/courses | List courses (paginated, filterable) |
| GET | /api/v1/courses/{id} | Get course by ID |
| GET | /api/v1/courses/slug/{slug} | Get course by slug |
| POST | /api/v1/courses | Create course (Admin) |
| PUT | /api/v1/courses/{id} | Update course (Admin) |
| DELETE | /api/v1/courses/{id} | Delete course (Admin) |
| GET | /api/v1/courses/search | Search (level, category, price) |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/categories | List categories |
| POST | /api/v1/categories | Create (Admin) |
| PUT | /api/v1/categories/{id} | Update (Admin) |

### Modules & Lessons
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/courses/{id}/modules | List modules of a course |
| POST | /api/v1/courses/{id}/modules | Create module (Admin) |
| GET | /api/v1/modules/{id}/lessons | List lessons |
| POST | /api/v1/modules/{id}/lessons | Create lesson (Admin) |

### Reviews
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/courses/{id}/reviews | List reviews (paginated) |
| POST | /api/v1/courses/{id}/reviews | Add review (authenticated) |

### Instructors
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/instructors | List instructors |
| GET | /api/v1/instructors/{id}/courses | List instructor's courses |
